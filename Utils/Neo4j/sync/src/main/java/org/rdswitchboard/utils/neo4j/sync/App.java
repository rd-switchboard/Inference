package org.rdswitchboard.utils.neo4j.sync;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class App {
	
	private static final String DEF_PATH_TMP = "tmp";
	private static final String DEF_PATH_ZIP = ".zip";
	private static final String DEF_SYNC_HOME = "sync";
	private static final String DEF_SYNC_PREFIX = "sync_";
	private static final String DEF_KEYS_LIST = "keys.list";
	private static final String DEF_SYNC_LEVEL = "3";	
	private static final String DEF_SOURCE_DB = "neo4j-source";
	private static final String DEF_TARGET_DB = "neo4j-target";
	private static final String DEF_NEO4J_DB = "neo4j";
	private static final String DEF_NEO4J_ZIP = "neo4j.zip";
		
	private static GraphDatabaseService srcGraphDb;
	private static GraphDatabaseService dstGraphDb;
	
	private static Path work;	
	private static Set<String> keys; 
	private static AmazonS3 s3client;
	private static Map<Long, Long> mapImported;
			
	private static final Label labelDataset = DynamicLabel.label(GraphUtils.TYPE_DATASET);
	private static final Label labelGrant = DynamicLabel.label(GraphUtils.TYPE_GRANT);
	private static final Label labelResearcher = DynamicLabel.label(GraphUtils.TYPE_RESEARCHER);
	private static final Label labelPublication = DynamicLabel.label(GraphUtils.TYPE_PUBLICATION);
	
	private static final RelationshipType relKnownAs = DynamicRelationshipType.withName(GraphUtils.RELATIONSHIP_KNOWN_AS);
	
	private static int syncLevel;
	private static long nodeCounter = 0;
	private static long relCounter = 0;
	private static long chunksCounter = 0;
	private static long chunkSize = 0;

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        System.out.println("Sync Neo4j database");

	        String syncHome = properties.getProperty(Configuration.PROPERTY_SYNC_HOME, DEF_SYNC_HOME);
	        if (StringUtils.isEmpty(syncHome))
	            throw new IllegalArgumentException("The Sync Home can not be empty");
	        System.out.println("Home: " + syncHome);

	        String source = properties.getProperty(Configuration.PROPERTY_SYNC_SOURCE);
	        if (StringUtils.isEmpty(source))
	            throw new IllegalArgumentException("Source Neo4j can not be empty");
	        System.out.println("Source Neo4j: " + source);

	        String target = properties.getProperty(Configuration.PROPERTY_SYNC_TARGET);
	        if (StringUtils.isEmpty(target))
	            throw new IllegalArgumentException("Target Neo4j can not be empty");
	        System.out.println("Target Neo4j: " + target);

	        String bucket = properties.getProperty(Configuration.PROPERTY_SYNC_BUCKET);
	        	        
	        syncLevel = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_SYNC_LEVEL, DEF_SYNC_LEVEL));
	        
	        String keysList = properties.getProperty(Configuration.PROPERTY_SYNC_KEYS, DEF_KEYS_LIST);

	        keys = new HashSet<String>();
	        keys.add(GraphUtils.PROPERTY_KEY);
	        
	        File keysFile = new File(keysList);
			if (keysFile.isFile()) {
				List<String> list = FileUtils.readLines(keysFile);
				for (String l : list) {
					String s = l.trim();
					if (!s.isEmpty())// && !s.equals(GraphUtils.PROPERTY_KEY))
						keys.add(s); 
				}
			}
		    
			mapImported = new HashMap<Long, Long>();
						
	        s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());

	        Path home = Paths.get(syncHome);
	        Files.createDirectories(home);
	        work = Files.createTempDirectory(home, DEF_SYNC_PREFIX);
	        
	        Path sourceDb = getPath(DEF_SOURCE_DB);
	        Path targetDb = getPath(DEF_TARGET_DB);
	        
	        System.out.println("Install source database");
	        downloadDatabase(source, sourceDb);
	        
	        System.out.println("Install target database");
	        downloadDatabase(target, targetDb);
	        
	        System.out.println("Connecting to source database");
	        srcGraphDb = Neo4jUtils.getReadOnlyGraphDb(sourceDb.toString());
	        
	        System.out.println("Connecting to target database");
	        dstGraphDb = Neo4jUtils.getReadOnlyGraphDb(targetDb.toString());

	        System.out.println("Create global operation's driver");
			GlobalGraphOperations global = Neo4jUtils.getGlobalOperations(dstGraphDb);
			
			Set<String> types = new HashSet<String>();
			types.add(GraphUtils.TYPE_DATASET);
			types.add(GraphUtils.TYPE_GRANT);
			types.add(GraphUtils.TYPE_RESEARCHER);
			types.add(GraphUtils.TYPE_PUBLICATION);
			
	        System.out.println("Create Constraints");
	        try ( Transaction tx = dstGraphDb.beginTx() ) {
	        	for (String type : types) {
	        		System.out.println("Creating Constraint on: " + type + "(key)");
	        		
	        		Neo4jUtils.createConstrant(dstGraphDb, DynamicLabel.label(type), GraphUtils.PROPERTY_KEY);
	        	}
		        
	        	tx.success();
	        }

	        System.out.println("Create Indexes");
	        try ( Transaction tx = srcGraphDb.beginTx() ) {
	        	Schema schema = srcGraphDb.schema();

        		for (String type : types) {
        			Label label = DynamicLabel.label(type);
        			
        			for (String key : keys) {
	        			boolean exists = false;
	        			for (IndexDefinition index : schema.getIndexes(label)) {
	        				for (String property : index.getPropertyKeys()) {
	        					if (property.equals(key))
	        						exists = true;
	        					break;
	        				}
	        				
	        				if (exists)
	        					break;
	        			}
	        			
	        			if (exists) {
	        				System.out.println("Creating Index on: " + type + "(" + key + ")");
	    	        		
		        			schema
		        				.indexFor(label)
		        				.on(key)
		        				.create();
	        			}
	        		}
        		}
	        		
	        	tx.success();
	        }

	        
	    /*    System.out.println("Create Indexes");
	        try ( Transaction tx = srcGraphDb.beginTx() ) {
	        	for (String key : keys) {
	        		Neo4jUtils.createIndex(srcGraphDb, DynamicLabel.label(key), GraphUtils.PROPERTY_KEY);
	        	}

	        	tx.success();	        	
	        }*/
	        
	        System.out.println("Sync nodes");
	        
	        try ( Transaction ignored = srcGraphDb.beginTx() ) 
			{
	        	Transaction tx = dstGraphDb.beginTx();
	        	try {
					ResourceIterable<Node> srcNodes = global.getAllNodes();
		        	for (Node srcNode : srcNodes) {
		        		
		        		syncNode(srcNode);
		        
		        		if (chunkSize > 1000) {
        					
        					chunkSize = 0;
        					++chunksCounter;

        					System.out.println("Writing " + chunksCounter + " chunk to database");
        				
        					tx.success();
        					tx.close();
        					tx = dstGraphDb.beginTx();			        					
        				}
		        	}
		        	
		        	nodeCounter += chunkSize;
		        	tx.success();
	        	} finally {
	        		tx.close();
	        	}
			}
	        
	        System.out.println("Created " + nodeCounter + " nodes and " + relCounter + " relationships");
	      	
	        
	        System.out.println("Shutdown database");
	        
	        srcGraphDb.shutdown();
	        srcGraphDb = null;
	        dstGraphDb.shutdown();
	        dstGraphDb = null;
	        
	        System.out.println("Archive database");
	        
	        Path zipFile = Paths.get(DEF_NEO4J_ZIP);
	        zipFile(targetDb, zipFile, DEF_NEO4J_DB);
	        
	        System.out.println("Publish database");
	        
	        if (!StringUtils.isEmpty(bucket))
	        	uploadDatabase(zipFile, bucket);
	              
	        
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}	
/*	
	private static void addProperty(Node node, String key, Object value) {
		if (node.hasProperty(key)) {
			Set<Object> set = new HashSet<Object>();
			addPropertyToSet(set, node.getProperty(key));
			addPropertyToSet(set, value);
			
			node.setProperty(key, getValue(set));
		} else
			node.setProperty(key, value);
	}
	
	protected static void addPropertyToSet(Set<Object> set, Object value) {
		if (value instanceof String[]) 
			set.addAll(Arrays.asList((String[]) value));
		else if (value instanceof Boolean[])
			set.addAll(Arrays.asList((Boolean[]) value));
		else if (value instanceof Byte[])
			set.addAll(Arrays.asList((Byte[]) value));
		else if (value instanceof Short[])
			set.addAll(Arrays.asList((Short[]) value));
		else if (value instanceof Integer[])
			set.addAll(Arrays.asList((Integer[]) value));
		else if (value instanceof Long[])
			set.addAll(Arrays.asList((Long[]) value));
		else if (value instanceof Float[])
			set.addAll(Arrays.asList((Float[]) value));
		else if (value instanceof Double[])
			set.addAll(Arrays.asList((Double[]) value));
		/*else if (value instanceof Object[])
			set.addAll(Arrays.asList((Object[]) value));* /
		else if (value instanceof Collection<?>)
			set.addAll((Collection<?>) value);
		else if (value instanceof Map<?,?>)
			throw new IllegalArgumentException("Maps as Parameters are not supported");
		else if (value.getClass().isArray())
			throw new IllegalArgumentException("Array myst be of Primitive type");
		else
			set.add(value);
	}
	
	protected static Object getValue(Set<Object> set) {
		if (null == set)
			return null;
		int size = set.size();
		if (0 == size)
			return null;
		
		Object element = set.iterator().next();
		if (1 == size)
			return element;
		if (element instanceof Boolean)
			return set.toArray(new Boolean[size]);
		if (element instanceof Byte)
			return set.toArray(new Byte[size]);
		if (element instanceof Short)
			return set.toArray(new Short[size]);
		if (element instanceof Integer)
			return set.toArray(new Integer[size]);
		if (element instanceof Long)
			return set.toArray(new Long[size]);
		if (element instanceof Float)
			return set.toArray(new Float[size]);
		if (element instanceof Double)
			return set.toArray(new Double[size]);
		if (element instanceof String)
			return set.toArray(new String[size]);
		
		throw new ClassCastException("Unable to convert Property Array, they property type: " + element.getClass() + " is not supported");
	}*/
	
	private static void copySyblings(Node src, Node dst, int synblingLevel) {
		// Iterate throigh all node relationships
		Iterable<Relationship> rels = src.getRelationships();
		for (Relationship rel : rels) {
			// find node sitting on other end of relationship
			Node other = rel.getOtherNode(src);
			Node copy = copyNode(other);
			
			if (!isRelated(dst, copy)) 
				dst.createRelationshipTo(copy, rel.getType());
			
			if (synblingLevel > 0)
				copySyblings(copy, dst, synblingLevel-1);			
		}
	}
	
	private static boolean isRelated(Node from, Node to) {
		Iterable<Relationship> rels = from.getRelationships();
		for (Relationship rel : rels) 
			if (rel.getOtherNode(from).getId() == to.getId()) 
				return true;
		
		return false;
	}
	
	private static Node copyNode(Node srcNode) {
		// first check did we already have imported that node
		Long id = mapImported.get(srcNode.getId());
		if (id != null)
			return dstGraphDb.getNodeById(id);
		
		// Acquire source node key and type
		// We are in the RDS ecosystem now, therefore all keys must be strings, 
		// all types must be valid and no additional checks should be required
		String srcKey = (String) srcNode.getProperty(GraphUtils.PROPERTY_KEY);
		String srcType = (String) srcNode.getProperty(GraphUtils.PROPERTY_TYPE);
		
		// Convert type to a proper node label
		Label type = DynamicLabel.label(srcType);
		
		// let try find same node in the dst database
		Node node = dstGraphDb.findNode(type, GraphUtils.PROPERTY_KEY, srcKey);
		if (node == null) {
			// if the node does not exists, create it
			node = dstGraphDb.createNode();
			
			// copy all node properties
			for (String p : srcNode.getPropertyKeys()) 
				node.setProperty(p, srcNode.getProperty(p));
			
			// copy all node labels
			for (Label l : srcNode.getLabels())
				node.addLabel(l);
			
			// increase nodes count
			++nodeCounter;
			
			// increase chunk syze
			++chunkSize; 
		}
		
		// store node id in the map, so we do not need to search it again
		mapImported.put(srcNode.getId(), node.getId());
		
		return node;
	}
	
	private static void copyRelationship(Node from, Node to, RelationshipType type) {
		// create relationship to the node if needed
		if (!isRelated(from, to)) {
			from.createRelationshipTo(to, type);
			
			// increase relationships count
			++relCounter;
			
			// increase chunk size
			++chunkSize; 
		}
	}
	
	private static void matchNode(Node dstNode, Label labelType, String property, Object value) {
		// At this point the sync will only match nodes of the same type. 
		// This will require source nodes to have correct type or sync program will not work
		ResourceIterator<Node> nodes = srcGraphDb.findNodes(labelType, property, value);
		if (null != nodes)
			while (nodes.hasNext()) {
				Node srcNode = nodes.next();

				// get or copy the node to the dst database
				Node cpyNode = copyNode(srcNode);
				
				// create relationships
				copyRelationship(dstNode, cpyNode, relKnownAs);
				
				// copy node syblings
				copySyblings(cpyNode, dstNode, syncLevel);				
			}
	}
	
	private static void syncNode(Node dstNode) throws Exception {
		// Node healty check 
		
		// a simple check to see if node has a key, source and type
		if (!dstNode.hasProperty(GraphUtils.PROPERTY_KEY) || 
			!dstNode.hasProperty(GraphUtils.PROPERTY_SOURCE) ||
			!dstNode.hasProperty(GraphUtils.PROPERTY_TYPE))
			return;
		
		// extract node type. The node must have one string type
		Object type = dstNode.getProperty(GraphUtils.PROPERTY_TYPE);
		if (type == null || !(type instanceof String))
			return;
		
		// the type must be either datatase, grant, researcher or publication
		Label labelType;
		if (type.equals(GraphUtils.TYPE_DATASET))
			labelType = labelDataset;
		else if (type.equals(GraphUtils.TYPE_GRANT))
			labelType = labelGrant;
		else if (type.equals(GraphUtils.TYPE_RESEARCHER))
			labelType = labelResearcher;
		else if (type.equals(GraphUtils.TYPE_PUBLICATION))
			labelType = labelPublication;
		else
			return;
		
		// check if node has one of property required for syncing 
		for (String property : keys) {
			if (dstNode.hasProperty(property)) {
				Object values = dstNode.getProperty(property);
				
				// we obly interesting in String or String[] properties at this point
				if (values instanceof String) 
					matchNode(dstNode, labelType, property, (String) values);
				else if (values instanceof String[])
	        		for (String value : (String[]) values)
	        			matchNode(dstNode, labelType, property, value);
			}
		}
	}
	
	private static boolean isZip(String path) {
		return path.trim().toLowerCase().endsWith(DEF_PATH_ZIP);
	}
	
	private static String getFolderName(String file) {
		int idx = file.toLowerCase().lastIndexOf(DEF_PATH_ZIP);
		if (idx < 0)
			throw new IllegalArgumentException("Expected ZIP archive name but got " + file);
		
		return file.substring(0, idx);
	}
	
	private static Path getPath(String path) {
		return Paths.get(work.toString(), path);
	}

	private static Path getTmpPath() {
		return getPath(DEF_PATH_TMP);
	}
	
	private static void downloadFileS3(S3Path path, Path output) throws FileNotFoundException, IOException {
		S3Object object = s3client.getObject(new GetObjectRequest(path.getBucket(), path.getKey()));
		byte[] buffer = new byte[1024];
		int n;
		
		try (InputStream is = object.getObjectContent()) {
			try (OutputStream os = new FileOutputStream(output.toFile())) {
				while((n = is.read(buffer)) > 0) {
				    os.write(buffer, 0, n); 
				}
			}
		}	
	}
	
	private static void zipFile(Path zipFile, Path input, String rootName) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
			zipEntry(zos, input, input, rootName);
		}
	}
	
	private static void zipEntry(ZipOutputStream zos, Path root, Path source, String rootName) throws IOException {
		if (Files.isDirectory(source)) {
	    	// if directory not exists, create it
			zos.putNextEntry(new ZipEntry(source.toString() + "/"));
			
    		// list all the directory contents
    		File files[] = source.toFile().listFiles();
    		for (File file : files) 
    			zipEntry(zos, root, file.toPath(), rootName);
	    } else {
	    	System.out.println("Zip File " + source);
	    	
	    	Path local = Paths.get(rootName, source.relativize(root).toString());
	    	ZipEntry ze = new ZipEntry(local.toString());
	    	
	    	zos.putNextEntry(ze);
	    	
	    	try (InputStream in = new FileInputStream(source.toString())) {
	    		byte[] buffer = new byte[1024];
    			int n;
    	        
				//copy the file content in bytes 
    			while ((n = in.read(buffer)) > 0) {
        			zos.write(buffer, 0, n);
        		}
    		}
	    	
	    	zos.closeEntry();
    	}
	}
	
	private static void unzipFile(Path zipFile, Path output) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
		
			Path base = Paths.get(getFolderName(zipFile.toFile().getName()));
			byte[] buffer = new byte[1024];
			Path file;
			int n;

			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
	        	file = Paths.get(ze.getName());
				
	        	if (file.toString().startsWith(base.toString()))
					file = base.relativize(file);

		        System.out.println("unzip : "+ file.toString());
	        	
				file = Paths.get(output.toString(), file.toString());

		        if (ze.isDirectory()) 
		        	Files.createDirectories(file);
	        	else {
		        	// create all non exists folders
		        	// else you will hit FileNotFoundException for compressed folder
				      
		            try (OutputStream os = new FileOutputStream(file.toFile())) {             
			        	while ((n = zis.read(buffer)) > 0) {
			        		os.write(buffer, 0, n);
			            }
		            }
		        }
		        		
	        	ze = zis.getNextEntry();
	    	}
		    	
		    zis.closeEntry();
		}
	}
	
	public static void copyFolder(Path src, Path dest) throws IOException{
	    if (Files.isDirectory(src)) {
	    	// if directory not exists, create it
	    	if (!Files.exists(dest)) {
	    		System.out.println("Create Directory " + dest);
	    		
	    		Files.createDirectories(dest);	    		
    		}
	    		
    		// list all the directory contents
    		String files[] = src.toFile().list();
    		
    		for (String file : files) {
    			// construct the src and dest file structure
    			Path srcFile = Paths.get(src.toString(), file);
    			Path destFile = Paths.get(dest.toString(), file);
    		  
    			// recursive copy
    			copyFolder(srcFile,destFile);
    		}
	    } else {
	    	System.out.println("Copy File " + dest);
	    	
    		// if file, then copy it
    		// Use bytes stream to support all file types
    		try (InputStream in = new FileInputStream(src.toFile())) {
    			try (OutputStream out = new FileOutputStream(dest.toFile())) { 
    	                     
    				byte[] buffer = new byte[1024];
    				int n;
    	        
    				//copy the file content in bytes 
    				while ((n = in.read(buffer)) > 0){
    					out.write(buffer, 0, n);
    				}
    			}
    		}
    	}
	}

	private static void downloadDatabase(String from, Path to) throws FileNotFoundException, IOException {
		System.out.println("Downloading database from " + from + " to " + to);
		S3Path path = S3Path.parse(from);
		if (null != path && path.isValud()) {
			System.out.println("The file is hosted on S3 bucket: " + path.getBucket() + ", key: " + path.getKey() + ", file: " + path.getFile());
			if (isZip(path.getFile())) {
				// the from path is a path to S3 file 
				Path tmp = Paths.get(getTmpPath().toString(), path.getFile());
				Files.createDirectories(tmp.getParent());
				
				System.out.println("Tmp path: " + tmp);
			
				downloadFileS3(path, tmp);
				unzipFile(tmp, to);
			} else 
				throw new IllegalArgumentException("Only Zip archives are supported for S3");
		} else {
			Path local = Paths.get(from);
			if (null == local || !Files.exists(local))
				throw new IllegalArgumentException("The local path are invalid: " + local.toString());
			if (Files.isDirectory(local))
				copyFolder(local, to);
			else if (isZip(local.toString())) 
				unzipFile(local, to);
			else 
				throw new IllegalArgumentException("The local path are invalid: " + local.toString());
				
		}			
	}
	
	private static void uploadDatabase(Path zipFile, String bucket) throws FileNotFoundException, IOException {
		try (InputStream is = new FileInputStream(zipFile.toFile())) {
        	
        	byte[] bytes = IOUtils.toByteArray(is);
        	
        	ObjectMetadata metadata = new ObjectMetadata();
        	metadata.setContentLength(bytes.length);
        	
        	InputStream inputStream = new ByteArrayInputStream(bytes);
        	
        	PutObjectRequest request = new PutObjectRequest(bucket, DEF_NEO4J_ZIP, inputStream, metadata);
        	
	        s3client.putObject(request);
        }  
	}
}
