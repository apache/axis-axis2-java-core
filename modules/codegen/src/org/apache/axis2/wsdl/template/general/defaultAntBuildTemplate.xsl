<xsl:stylesheet version="1.0" xmlns:xalan="http://xml.apache.org/xslt"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" xalan:indent-amount="4"/>
    <xsl:template match="/ant">
        <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>

        <project basedir="." default="jar.all">
            <xsl:comment>Auto generated ant build file</xsl:comment>
            <property name="src">
                <xsl:attribute name="value">${basedir}/src</xsl:attribute>
            </property>
            <property name="test">
                <xsl:attribute name="value">${basedir}/test</xsl:attribute>
            </property>
            <property name="build">
                <xsl:attribute name="value">${basedir}/build</xsl:attribute>
            </property>
            <property name="classes">
                <xsl:attribute name="value">${build}/classes</xsl:attribute>
            </property>
            <property name="lib">
                <xsl:attribute name="value">${build}/lib</xsl:attribute>
            </property>
            <property name="resources">
                <xsl:attribute name="value">${basedir}/resources</xsl:attribute>
            </property>
            <property name="name">
                <xsl:attribute name="value"><xsl:value-of select="@servicename"/></xsl:attribute>
            </property>

            <property name="jars.ok" value=""></property>

            <target name="init">
                <mkdir>
                    <xsl:attribute name="dir">${build}</xsl:attribute>
                </mkdir>
                <mkdir>
                    <xsl:attribute name="dir">${classes}</xsl:attribute>
                </mkdir>
                <mkdir>
                    <xsl:attribute name="dir">${lib}</xsl:attribute>
                </mkdir>
            </target>

            <target name="pre.compile.test" depends="init">
                <xsl:comment>Test the classpath for the availability of necesary classes</xsl:comment>

                <available classname="javax.xml.stream.XMLStreamReader" property="stax.available"/>
                <available classname="org.apache.axis2.engine.AxisEngine" property="axis2.available"/>
                <condition property="jars.ok" >
                    <and>
                        <isset property="stax.available"/>
                        <isset property="axis2.available"/>
                    </and>
                </condition>

                <xsl:comment>Print out the availabilities</xsl:comment>
                <echo>
                     <xsl:attribute name="message">Stax Availability= ${stax.available}</xsl:attribute>
                </echo>
                <echo>
                     <xsl:attribute name="message">Axis2 Availability= ${axis2.available}</xsl:attribute>
                </echo>

            </target>

            <target name="compile.all" depends="pre.compile.test">
                <xsl:attribute name="if">jars.ok</xsl:attribute>
                <javac debug="on">
                    <xsl:attribute name="destdir">${classes}</xsl:attribute>
                    <xsl:attribute name="srcdir">${src}</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="location">${bin}/${xbeans.packaged.jar.name}</xsl:attribute>
                    </classpath>
                    <classpath>
                        <xsl:attribute name="location">${java.class.path}</xsl:attribute>
                    </classpath>
                </javac>
            </target>

            <target name="compile.test" depends="pre.compile.test,compile.all">
                <xsl:attribute name="if">jars.ok</xsl:attribute>
                <mkdir>
                    <xsl:attribute name="dir">${test}</xsl:attribute>
                </mkdir>
                <javac debug="on">
                    <xsl:attribute name="destdir">${classes}</xsl:attribute>
                    <xsl:attribute name="srcdir">${test}</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="location">${bin}/${xbeans.packaged.jar.name}</xsl:attribute>
                    </classpath>
                    <classpath>
                        <xsl:attribute name="location">${java.class.path}</xsl:attribute>
                    </classpath>
                </javac>
            </target>

            <target name="echo.classpath.problem" depends="pre.compile.test">
                <xsl:attribute name="unless">jars.ok</xsl:attribute>
                <echo message="The class path is not set right!
                               Please make sure the following classes are in the classpath
                               1. Stax
                               2. Axis2
                "></echo>
            </target>

            <!--<target name="jar.all" depends="compile.all,echo.classpath.problem">
                <xsl:attribute name="if">jars.ok</xsl:attribute>
                <jar>
                    <xsl:attribute name="basedir">${classes}</xsl:attribute>
                    <xsl:attribute name="destfile">${lib}/${name}.jar</xsl:attribute>
                </jar>
            </target>-->

            <target name="jar.client" depends="compile.test,echo.classpath.problem" if="jars.ok">
               <jar>
                    <xsl:attribute name="destfile">${lib}/${name}-client.jar</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${classes}</xsl:attribute>
                        <exclude>
                            <xsl:attribute name="name">**/META-INF/*.*</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/lib/*.*</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/*MessageReceiver.class</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/*Skeleton.class</xsl:attribute>
                        </exclude>
                    </fileset>
                </jar>
            </target>

            <target name="jar.client.test.omit" depends="compile.all,echo.classpath.problem" if="jars.ok">
                <jar>
                    <xsl:attribute name="destfile">${lib}/${name}-client.jar</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${classes}</xsl:attribute>
                        <exclude>
                            <xsl:attribute name="name">**/META-INF/*.*</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/lib/*.*</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/*MessageReceiver.class</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/*Skeleton.class</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/*Test.class</xsl:attribute>
                        </exclude>
                    </fileset>
                </jar>
            </target>

            <target name="jar.server" depends="compile.all,echo.classpath.problem">
                <xsl:attribute name="if">jars.ok</xsl:attribute>
                <copy>
                    <xsl:attribute name="toDir">${classes}/META-INF</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${resources}</xsl:attribute>
                        <include>
                            <xsl:attribute name="name">*.xml</xsl:attribute>
                        </include>
                        <include>
                            <xsl:attribute name="name">*.wsdl</xsl:attribute>
                        </include>
                    </fileset>
                </copy>
                <jar>
                    <xsl:attribute name="destfile">${lib}/${name}.aar</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="excludes">**/Test.class</xsl:attribute>
                        <xsl:attribute name="dir">${classes}</xsl:attribute>
                    </fileset>
                </jar>
            </target>

            <target name="jar.all" depends="jar.server,jar.client"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
