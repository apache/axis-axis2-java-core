/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.databinding.toJava;

import org.apache.axis2.databinding.gen.Generator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * Emitter knows about WSDL writers, one each for PortType, Binding, Service,
 * Definition, Type.  But for some of these WSDL types, Wsdl2java generates
 * multiple files.  Each of these files has a corresponding writer that extends
 * JavaWriter.  So the Java WSDL writers (JavaPortTypeWriter, JavaBindingWriter,
 * etc.) each calls a file writer (JavaStubWriter, JavaSkelWriter, etc.) for
 * each file that that WSDL generates.
 * <p/>
 * <p>For example, when Emitter calls JavaWriterFactory for a Binding Writer, it
 * returns a JavaBindingWriter.  JavaBindingWriter, in turn, contains a
 * JavaStubWriter, JavaSkelWriter, and JavaImplWriter since a Binding may cause
 * a stub, skeleton, and impl template to be generated.
 * <p/>
 * <p>Note that the writers that are given to Emitter by JavaWriterFactory DO NOT
 * extend JavaWriter.  They simply implement Writer and delegate the actual
 * task of writing to extensions of JavaWriter.
 * <p/>
 * <p>All of Wsdl2java's Writer implementations follow a common behaviour.
 * JavaWriter is the abstract base class that dictates this common behaviour.
 * This behaviour is primarily placed within the generate method.  The generate
 * method calls, in succession (note:  the starred methods are the ones you are
 * probably most interested in):
 * <dl>
 * <dt> * getFileName
 * <dd> This is an abstract method that must be implemented by the subclass.
 * It returns the fully-qualified file name.
 * <dt> isFileGenerated(file)
 * <dd> You should not need to override this method.  It checks to see whether
 * this file is in the List returned by emitter.getGeneratedFileNames.
 * <dt> registerFile(file)
 * <dd> You should not need to override this method.  It registers this file by
 * calling emitter.getGeneratedFileInfo().add(...).
 * <dt> * verboseMessage(file)
 * <dd> You may override this method if you want to provide more information.
 * The generate method only calls verboseMessage if verbose is turned on.
 * <dt> getPrintWriter(file)
 * <dd> You should not need to override this method.  Given the file name, it
 * creates a PrintWriter for it.
 * <dt> * writeFileHeader(pw)
 * <dd> You may want to override this method.  The default implementation
 * generates nothing.
 * <dt> * writeFileBody(pw)
 * <dd> This is an abstract method that must be implemented by the subclass.
 * This is where the body of a file is generated.
 * <dt> * writeFileFooter(pw)
 * <dd> You may want to override this method.  The default implementation
 * generates nothing.
 * <dt> closePrintWriter(pw)
 * <dd> You should not need to override this method.  It simply closes the
 * PrintWriter.
 * </dl>
 */
public abstract class JavaWriter implements Generator {

    /** This controls how many characters per line for javadoc comments */
    protected final static int LINE_LENGTH = 65;

    /** Field emitter */
    protected Emitter emitter;

    /** Field type */
    protected String type;

    /**
     * Constructor.
     *
     * @param emitter
     * @param type
     */
    protected JavaWriter(Emitter emitter, String type) {
        this.emitter = emitter;
        this.type = type;
    }    // ctor

    /**
     * Generate a file.
     *
     * @throws IOException
     */
    public void generate() throws IOException {

        String file = getFileName();

        if (isFileGenerated(file)) {
           // throw new DuplicateFileException("");
                //    Messages.getMessage("duplicateFile00", file), file);
        }

        registerFile(file);

        if (emitter.isVerbose()) {
            String msg = verboseMessage(file);

            if (msg != null) {
                System.out.println(msg);
            }
        }

        PrintWriter pw = getPrintWriter(file);

        writeFileHeader(pw);
        writeFileBody(pw);
        writeFileFooter(pw);
        closePrintWriter(pw);
    }    // generate

    /**
     * This method must be implemented by a subclass.  It
     * returns the fully-qualified name of the file to be
     * generated.
     *
     * @return
     */
    protected abstract String getFileName();

    /**
     * You should not need to override this method. It checks
     * to see whether the given file is in the List returned
     * by emitter.getGeneratedFileNames.
     *
     * @param file
     * @return
     */
    protected boolean isFileGenerated(String file) {
        return emitter.getGeneratedFileNames().contains(file);
    }    // isFileGenerated

    /**
     * You should not need to override this method.
     * It registers the given file by calling
     * emitter.getGeneratedFileInfo().add(...).
     *
     * @param file
     */
    protected void registerFile(String file) {
        emitter.getGeneratedFileInfo().add(file, null, type);
    }    // registerFile

    /**
     * Return the string:  "Generating <file>".  Override this
     * method if you want to provide more information.
     *
     * @param file
     * @return
     */
    protected String verboseMessage(String file) {
        return "";// todo Messages.getMessage("generating", file);
    }    // verboseMessage

    /**
     * You should not need to override this method.
     * Given the file name, it creates a PrintWriter for it.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    protected PrintWriter getPrintWriter(String filename) throws IOException {

        File file = new File(filename);
        File parent = new File(file.getParent());

        parent.mkdirs();

        return new PrintWriter(new FileWriter(file));
    }                                // getPrintWriter

    /**
     * This method is intended to be overridden as necessary
     * to generate file header information.  This default
     * implementation does nothing.
     *
     * @param pw
     * @throws IOException
     */
    protected void writeFileHeader(PrintWriter pw)
            throws IOException {
    }    // writeFileHeader

    /**
     * This method must be implemented by a subclass.  This
     * is where the body of a file is generated.
     *
     * @param pw
     * @throws IOException
     */
    protected abstract void writeFileBody(PrintWriter pw) throws IOException;

    /**
     * You may want to override this method.  This default
     * implementation generates nothing.
     *
     * @param pw
     * @throws IOException
     */
    protected void writeFileFooter(PrintWriter pw)
            throws IOException {
    }    // writeFileFooter

    /**
     * Close the print writer.
     *
     * @param pw
     */
    protected void closePrintWriter(PrintWriter pw) {
        pw.close();
    }    // closePrintWriter

    /**
     * Takes out new lines and wraps at Javadoc tags
     * @param documentation the raw comments from schema
     * @param addTab if true adds a tab character when wrapping (methods)
     */
    protected String getJavadocDescriptionPart(String documentation, boolean addTab) {
        if (documentation == null) {
            return "";
        }

        String doc = documentation.trim();

        if (documentation.trim().length() == 0) {
            //nothing to do
            return doc;
        }

        // make @ tags start a new line (for javadoc tags mostly)
        StringTokenizer st = new StringTokenizer(doc, "@");
        StringBuffer newComments;
        if (st.hasMoreTokens()) {
            String token = st.nextToken();
            boolean startLine = Character.isWhitespace(token.charAt(token.length() - 1))
                && (token.charAt(token.length() - 1) != '\n');
            newComments = new StringBuffer(token);
            
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                // don't span links across lines
                if (startLine) {
                    newComments.append('\n');
                }
                newComments.append('@');
                startLine = Character.isWhitespace(token.charAt(token.length() - 1))
            & (token.charAt(token.length() - 1) != '\n');

                newComments.append(token);
            }
        } else {
            newComments = new StringBuffer(doc);
        }
        newComments.insert(0, addTab ? "     * " : " * ");

        // tweak comment ending tags by insterting a
        // space between the star and the slash, BUG13407
        int pos = newComments.toString().indexOf("*/");
        while (pos >= 0) {
            newComments.insert(pos + 1, ' ');
            pos = newComments.toString().indexOf("*/");
        }
        
        // now pretty it up based on column length
        int lineStart = 0;
        int newlinePos = 0;
        while (lineStart < newComments.length()) {
            newlinePos = newComments.toString().indexOf("\n", lineStart);
            if (newlinePos == -1) {
                newlinePos = newComments.length();
            }
            if ((newlinePos - lineStart) > LINE_LENGTH) {
                // find first whitespace after length
                lineStart += LINE_LENGTH;
                while ((lineStart < newComments.length()) 
                    && !Character.isWhitespace(newComments.charAt(lineStart))) {
                    lineStart++;
                }

                if (lineStart < newComments.length()) {
                    // don't insert if line wold break at EOF
                    char next = newComments.charAt(lineStart);
                    // insert new line header
                    if ((next == '\r') || (next == '\n')) {
                        //newline exists at the break point, don't put in another one
                        newComments.insert(lineStart + 1, addTab ? "     * " : " * ");
                        lineStart += addTab ? 8 : 4;
                    } else {
                        newComments.insert(lineStart, addTab ? "\n     * " : "\n * ");
                        lineStart += addTab ? 8 : 4;
                    }
                }

                // chew up witespace after newline
                while ((lineStart < newComments.length()) 
                    && (newComments.charAt(lineStart) == ' ')) { // only chew up simple spaces
                    newComments.delete(lineStart, lineStart + 1);
                }
            } else {
                if (++newlinePos < newComments.length()) {
                    newComments.insert(newlinePos, addTab ? "     * " : " * ");
                }
                lineStart = newlinePos;
                lineStart += addTab ? 7 : 3;
            }
        }

        return newComments.toString();
    }

    /**
     * Output a documentation element as a Java comment.
     *
     * @param pw
     * @param element
     */
    protected void writeComment(PrintWriter pw, Element element) {
       writeComment(pw, element, true);
    }

    /**
     * Output a documentation element as a Java comment.
     *
     * @param pw
     * @param element
     * @param addTab
     */
    protected void writeComment(PrintWriter pw, Element element, boolean addTab) {

        if (element == null) {
            return;
        }

        Node child = element.getFirstChild();

        if (child == null) {
            return;
        }

        String comment = child.getNodeValue();

        if (comment != null) {
            int start = 0;

            pw.println();    // blank line

            pw.println(addTab ? "    /**" : "/**");
            pw.println(getJavadocDescriptionPart(comment, addTab));
            pw.println(addTab ? "     */" : " */");
        }
    }                        // writeComment
}    // abstract class JavaWriter
