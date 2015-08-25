package com.sxj.ldap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class Property
{
    /**
     * Loads the properties from the given fileName.  It first checks the
     * CLASSPATH for the location of the file, and then checks the fully
     * qualified path to the file if it was unable to load it.  So, if you made
     * the fileName parameter "/etc/somefile.properties", it would attempt to
     * load the file from "somewhere_on_the_filesystem/etc/somefile.properties".
     *  Where somewhere_on_the_filesystem is a directory in the CLASSPATH.  If
     * that could not be found, then it will attempt "/etc/somefile.properties".
     * If unable to load the properties it returns null.  For further
     * information on loading files from the CLASSPATH, see
     * Class.getResourceAsStream();
     *
     * @param fileName the filename to load properties from
     *
     * @return a Properties object if successful or null otherwise
     */
    public static Properties loadProperties(String fileName)
    { // BEGIN loadProperties(String)
        /**
         * Get all the properties we need from the message_store_factory.properties
         * file in.
         */
        Properties properties = null;
        InputStream inputStream = null;
        
        try
        {
            inputStream = Property.class.getResourceAsStream(fileName);
            if (inputStream != null)
            { // found it in the classpath
                properties = new Properties();
                properties.load(inputStream);
            }
            else
            { // attempt loading from a file using FileInputStream
                inputStream = new FileInputStream(fileName);
                if (inputStream != null)
                {
                    properties = new Properties();
                    properties.load(inputStream);
                }
                
            }
        }
        catch (FileNotFoundException fileNotFoundException)
        {
            /**
             * we don't care about this, we just return null.  In other words
             * the same behaviour as loadProperties(String).
             */
        }
        catch (Exception exception)
        {
            throw new RuntimeException("error loading property file : "
                    + fileName + " : " + exception.getMessage());
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (Exception exception)
                {
                }
                finally
                {
                }
            }
        }
        
        return properties;
    } // END loadProperties(String)
}
