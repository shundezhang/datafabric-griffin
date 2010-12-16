/*
 * GridfsConstants.java
 * 
 * Constants and defaults for the MongoDB GridFS storage backend.
 * 
 * Created: 2010-10-07 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * Changed:
 * 
 * Copyright (C) 2010 Australian Research Collaboration Service
 *                    and Auckland University of Technology, New Zealand
 * 
 * Some rights reserved
 * 
 * http://www.arcs.org.au/
 * http://www.aut.ac.nz/
 */
 
package au.org.arcs.griffin.filesystem.impl.gridfs;


/**
 * Constants and defaults for the MongoDB GridFS storage backend.
 *
 * @author Guy K. Kloss
 */
public class GridfsConstants {
    public static final String FILE_SEP = "/";
    public static final int DEFAULT_MONGO_PORT = 27017;
    public static final String SERVER_TYPE = "gridfs";
    public static final String BUCKET_NAME = "fs";
    public static final String DEFAULT_MONGO_HOST = "localhost";
}
