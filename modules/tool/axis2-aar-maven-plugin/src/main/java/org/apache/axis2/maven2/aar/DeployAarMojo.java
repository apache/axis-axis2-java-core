/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.maven2.aar;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.io.entity.EntityUtils;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Deploys an AAR to the Axis2 server.
 */
@Mojo(name = "deployaar", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class DeployAarMojo extends AbstractAarMojo {

    private final static String LOGIN_FAILED_ERROR_MESSAGE = "Invalid auth credentials!";

    /**
     * The URL of the Axis2 administration console.
     */
    @Parameter(defaultValue = "http://localhost:8080/axis2/axis2-admin", property = "axis2.aar.axis2AdminConsoleURL")
    private URL axis2AdminConsoleURL;

    /**
     * The administrator user name for the Axis2 administration console.
     */
    @Parameter(property = "axis2.aar.axis2AdminUser")
    private String axis2AdminUser;

    /**
     * The administrator password for the Axis2 administration console.
     */
    @Parameter(property = "axis2.aar.axis2AdminPassword")
    private String axis2AdminPassword;

    /**
     * Executes the DeployAarMojo on the current project.
     *
     * @throws MojoExecutionException if an error occurred while building the webapp
     */
    public void execute() throws MojoExecutionException {
        getLog().info("Deploying AAR artifact "+project.getArtifact().getFile()+" to Axis2 Web Console "+axis2AdminConsoleURL);
        try {
            deploy(project.getArtifact().getFile());
        } catch(MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Error deploying aar", e);
        }
    }

    /**
     * Deploys the AAR.
     *
     * @param aarFile the target AAR file
     * @throws MojoExecutionException
     * @throws HttpException
     * @throws IOException
     */
    private void deploy(File aarFile) throws MojoExecutionException, IOException {
        if(axis2AdminConsoleURL == null) {
            throw new MojoExecutionException("No Axis2 administrative console URL provided.");
        }

        // log into Axis2 administration console
        URL axis2AdminConsoleLoginURL = new URL(axis2AdminConsoleURL.toString()+"/login");
        // TODO get name of web service mount point
	HttpPost httpPost = new HttpPost(axis2AdminConsoleLoginURL.toString());
	List<NameValuePair> nvps = new ArrayList<>();
	nvps.add(new BasicNameValuePair("userName", axis2AdminUser));
	nvps.add(new BasicNameValuePair("password", axis2AdminPassword));
	httpPost.setEntity(new UrlEncodedFormEntity(nvps));

	CloseableHttpClient httpclient = HttpClients.createDefault();
	//httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
        //        CookiePolicy.BROWSER_COMPATIBILITY);

        try {
            CloseableHttpResponse hcResponse = httpclient.execute(httpPost);
	    int status = hcResponse.getCode();
            if(status != 200) {
                throw new MojoExecutionException("Failed to log in");
            }
	    HttpEntity responseEntity = hcResponse.getEntity();
            if(responseEntity==null) {
                throw new MojoExecutionException("url request returned null entity: " + new StatusLine(hcResponse));
            }
            String responseStr = EntityUtils.toString(responseEntity);
            if(responseStr.indexOf(LOGIN_FAILED_ERROR_MESSAGE)!=-1) {
                throw new MojoExecutionException("Failed to log into Axis2 administration web console using credentials");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error deploying aar", e);
        }finally {
            httpclient.close();
        }

        // deploy AAR web service
        URL axis2AdminConsoleUploadURL = new URL(axis2AdminConsoleURL.toString()+"/upload");
        getLog().debug("Uploading AAR to Axis2 Admin Web Console "+axis2AdminConsoleUploadURL);

	MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.LEGACY);
        File file = project.getArtifact().getFile();
        FileBody fileBody = new FileBody(file);
        builder.addPart(project.getArtifact().getFile().getName(), fileBody);

	httpPost = null;
	httpPost = new HttpPost(axis2AdminConsoleLoginURL.toString());

	httpclient = null;
	httpclient = HttpClients.createDefault();

        try {
            CloseableHttpResponse hcResponse = httpclient.execute(httpPost);
	    int status = hcResponse.getCode();
            if(status != 200) {
                throw new MojoExecutionException("Failed to log in");
            }
        }finally {
            httpclient.close();
        }

        // log out of web console
        URL axis2AdminConsoleLogoutURL = new URL(axis2AdminConsoleURL.toString()+"/logout");
        getLog().debug("Logging out of Axis2 Admin Web Console "+axis2AdminConsoleLogoutURL);

        HttpGet get = new HttpGet(axis2AdminConsoleLogoutURL.toString());

	httpclient = null;
	httpclient = HttpClients.createDefault();
	/* AXIS2-6051 in httpclient5 / core5 it is now CookieSpec that 
	 * is set in RequestConfig. The default is 'Lax' which seems equivalent
	 * to the deprecated property BROWSER_COMPATIBILITY: 
	 * The policy that provides high degree of compatibilty with common cookie management of popular HTTP agents. 
	httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.BROWSER_COMPATIBILITY);
        */

        try {
            CloseableHttpResponse hcResponse = httpclient.execute(get);
	    int status = hcResponse.getCode();
            if(status != 200) {
                throw new MojoExecutionException("Failed to log out");
            }
        }finally {
            httpclient.close();
        }

    }

}
