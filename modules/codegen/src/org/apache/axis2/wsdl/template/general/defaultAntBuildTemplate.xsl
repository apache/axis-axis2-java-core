<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="/ant">
        <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>

        <project basedir="." default="move.files">
            <xsl:comment>Auto generated ant build file</xsl:comment>
            <property name="src">
                <xsl:attribute name="value">${basedir}\src</xsl:attribute>
            </property>
            <property name="classes">
                <xsl:attribute name="value">${basedir}\classes</xsl:attribute>
            </property>
            <property name="bin">
                <xsl:attribute name="value">${basedir}\bin</xsl:attribute>
            </property>
            <property name="other">
                <xsl:attribute name="value">${basedir}\other</xsl:attribute>
            </property>

            <!--<property name="xbeans.available" value=""></property>
            <property name="stax.available" value=""></property>
            <property name="axis2.available" value=""></property>-->
            <property name="jars.ok" value=""></property>
            <property name="mappings.folder.name" value="Mapping"></property>
            <property name="schemas.folder.name" value="schemas"></property>


            <target name="move.files" depends="init">
                <xsl:comment>first move the generated packages</xsl:comment>
                <move>
                    <xsl:attribute name="todir">${src}</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${basedir}</xsl:attribute>
                        <xsl:attribute name="includes"><xsl:value-of select="$package"></xsl:value-of>\**\</xsl:attribute>
                    </fileset>

                </move>
                <xsl:comment>move the rest of the stuff to the other folder</xsl:comment>
                <move>
                    <xsl:attribute name="todir">${other}</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${basedir}</xsl:attribute>
                        <xsl:attribute name="includes">${mappings.folder.name}\**\</xsl:attribute>
                    </fileset>
                </move>
                <move>
                    <xsl:attribute name="todir">${other}</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${basedir}</xsl:attribute>
                        <xsl:attribute name="includes">${schemas.folder.name}\**\</xsl:attribute>
                    </fileset>
                </move>

            </target>

            <target name="init">
                <mkdir>
                    <xsl:attribute name="dir">${src}</xsl:attribute>
                </mkdir>
                <mkdir>
                    <xsl:attribute name="dir">${classes}</xsl:attribute>
                </mkdir>
                <mkdir>
                    <xsl:attribute name="dir">${bin}</xsl:attribute>
                </mkdir>

            </target>

            <target name="pre.compile.test" depends="move.files">
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
                <xsl:attribute name="if">${jars.ok}</xsl:attribute>
                <javac>
                    <xsl:attribute name="destdir">${classes}</xsl:attribute>
                    <xsl:attribute name="srcdir">${src}</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="location">${bin}\${xbeans.packaged.jar.name}</xsl:attribute>
                    </classpath>
                    <classpath>
                        <xsl:attribute name="location">${java.class.path}</xsl:attribute>
                    </classpath>
                </javac>
            </target>

            <target name="echo.classpath.problem" depends="pre.compile.test">
                <xsl:attribute name="unless">${jars.ok}</xsl:attribute>
                <echo message="The class path is not set right!
                               Please make sure the following classes are in the classpath
                               1. Stax
                               2. Axis2
                "></echo>
            </target>
            <target name="jar.all" depends="compile.all,echo.classpath.problem">
                <xsl:attribute name="if">${jars.ok}</xsl:attribute>
                <jar>
                    <xsl:attribute name="basedir">${classes}</xsl:attribute>
                    <xsl:attribute name="destfile">${bin}</xsl:attribute>
                </jar>
            </target>
        </project>
    </xsl:template>
</xsl:stylesheet>
