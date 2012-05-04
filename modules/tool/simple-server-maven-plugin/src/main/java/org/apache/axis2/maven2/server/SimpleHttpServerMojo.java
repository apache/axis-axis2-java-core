/*
 * Copyright 2004, 2009 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.maven2.server;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.axis2.maven2.server.util.Axis2Server;
import org.apache.axis2.maven2.server.util.RepoHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;

import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_PORT;
import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_CLASSES_DIRECTORY;
import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_TEST_CLASSES_DIRECTORY;
import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_REPO_LOCATION;
import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_CONF_DIR;
import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_CONF_FILE_NAME;

/**
 * Run simple Axis 2Server.
 * 
 * @since 1.7.0
 * @goal run 
 * @execute phase="compile"  // TODO - check this again.
 * @requiresDependencyResolution runtime 
 */
public class SimpleHttpServerMojo extends AbstractMojo {

    // configuration parameters.
    /**
     * The repository path.
     * 
     * @parameter
     */
    private String repoPath;

    /**
     * Path to axis2.xml configuration file.
     * 
     * @parameter
     */
    private String confPath;

    /**
     * This parameter indicate service type whether it's JAX-WS or not.
     * 
     * @parameter default-value="false"
     */
    private boolean jaxwsService;

    /**
     * @parameter
     */
    private String stdServiceSrcDir;

    /**
     * @parameter
     */
    private String jaxwsServiceSrcDir;

    /**
     * @parameter
     */
    private String moduleSrcDir;

    /**
     * @parameter 
     */
    private String port;
    
    /**
     * Indicates whether to fork the server.
     * 
     * @parameter default-value="false"
     */
    private boolean fork;
    
    /**
     * @parameter default-value="1024"
     */
    private int dataBufferSize;

    /*
     * Maven project parameters
     */

    /**
     * The plugin descriptor
     * 
     * @parameter default-value="${descriptor}"
     * @required
     */
    private PluginDescriptor descriptor;

    /**
     * Build directory of current project.
     * 
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private String buildDir;

    /**
     * Project version
     * 
     * @parameter default-value="${project.version}"
     * @required
     * @readonly
     */
    private String projectVersion;

    /**
     * Project Id
     * 
     * @parameter default-value="${project.artifactId}"
     * @required
     * @readonly
     */
    private String projectId;

    private Axis2Server server;
   

    public RepoHelper getRepoHelper() {
        RepoHelper repoHelper = new RepoHelper(repoPath, getLog());
        if (stdServiceSrcDir != null) {
            repoHelper.setStdServiceSrcDir(stdServiceSrcDir);
        }
        if (jaxwsServiceSrcDir != null) {
            repoHelper.setJaxwsServiceSrcDir(jaxwsServiceSrcDir);
        }
        if (moduleSrcDir != null) {
            repoHelper.setModuleSrcDir(moduleSrcDir);
        }
        if (jaxwsService) {
            repoHelper.setJaxwsService(jaxwsService);
            String serviceJarFile = buildDir + File.separator + projectId + "-" + projectVersion
                    + ".jar";
            repoHelper.setServiceJarLocation(serviceJarFile);
        }
        if(dataBufferSize > 0){
        	repoHelper.setDataBufferSize(dataBufferSize);
        } else {
        	repoHelper.setDataBufferSize(org.apache.axis2.Constants.Configuration.DEFAULT_DATA_BUFFER_SIZE);
        }
        return repoHelper;
    }

    public void execute() throws MojoExecutionException {
        try {
            getLog().info("repo path : " + repoPath);
            getLog().info("conf path : " + confPath);
            getRepoHelper().prepareRepostory();
            extendClassLoader();
            String serverPort = port == null ? DEFAULT_PORT : port;            
            server = Axis2Server.newInstance(repoPath, checkFordefaultConfFile(confPath), serverPort, getLog());
            if (fork) {
                new Thread(new Runnable() {
                    public void run() {
                        getLog().info(" Starting Axis2 Simple HTTP Server in a foke mode................. ");
                        server.startServer();
                        getLog().info(" Axis2 Simple HTTP server satreted");
                        waitForShutdown();
                    }
                }).start();
            } else {
                server.startServer();
                waitForShutdown();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to start AXIS2 HTTP server", e);
        }

    }

    private void extendClassLoader() throws DuplicateRealmException, MalformedURLException {
        ClassRealm realm = descriptor.getClassRealm();
        if (realm == null) {
            ClassWorld world = new ClassWorld();
            realm = world.newRealm("maven.plugin." + getClass().getSimpleName(), Thread
                    .currentThread().getContextClassLoader());
        }
        File cls = new File(buildDir + File.separator + DEFAULT_CLASSES_DIRECTORY);
        File testCls = new File(buildDir + File.separator + DEFAULT_TEST_CLASSES_DIRECTORY);
        realm.addURL(cls.toURI().toURL());
        realm.addURL(testCls.toURI().toURL());
        Thread.currentThread().setContextClassLoader(realm);
    }

    protected void waitForShutdown() {
        final boolean[] shutdown = new boolean[] { false };
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                synchronized (shutdown) {
                    shutdown[0] = true;
                    shutdown.notify();
                }
            }
        });

        // Wait for any shutdown event
        synchronized (shutdown) {
            while (!shutdown[0]) {
                try {
                    shutdown.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        server.stopServer();
        getLog().info("Axis2 Simple HTTP server stoped ");

    }
    
    private String checkFordefaultConfFile(String inPath) {
        if (inPath != null) {
            return inPath;
        } else {
            String path = repoPath != null ? repoPath : DEFAULT_REPO_LOCATION;
            path = path + File.separator + DEFAULT_CONF_DIR + File.separator
                    + DEFAULT_CONF_FILE_NAME;
            File confFile = new File(path);
            if(confFile.exists()){
                return path;
            }
            return null;
        }
    }

}
