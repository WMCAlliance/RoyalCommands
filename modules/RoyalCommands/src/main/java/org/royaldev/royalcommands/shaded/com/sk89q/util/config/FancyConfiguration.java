/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.shaded.com.sk89q.util.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML configuration loader. To use this class, construct it with path to
 * a file and call its load() method. For specifying node paths in the
 * various get*() methods, they support SK's path notation, allowing you to
 * select child nodes by delimiting node names with periods.
 * <br>
 * <p>
 * For example, given the following configuration file:</p>
 * <br>
 * <pre>members:
 *     - Hollie
 *     - Jason
 *     - Bobo
 *     - Aya
 *     - Tetsu
 * worldguard:
 *     fire:
 *         spread: false
 *         blocks: [cloth, rock, glass]
 * sturmeh:
 *     cool: false
 *     eats:
 *         babies: true</pre>
 * <br>
 * <p>Calling code could access sturmeh's baby eating state by using
 * <code>getBoolean("sturmeh.eats.babies", false)</code>. For lists, there are
 * methods such as <code>getStringList</code> that will return a type safe list.
 * <br>
 * <p>This class is currently incomplete. It is not yet possible to get a node.
 * </p>
 */

public class FancyConfiguration extends ConfigurationNode {

    private final Yaml yaml;
    private final File file;
    private String header = null;

    public FancyConfiguration(File file) {
        super(new HashMap<String, Object>());
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(new SafeConstructor(new LoaderOptions()), new EmptyNullRepresenter(), options);
        this.file = file;
    }

    /**
     * This method returns an empty ConfigurationNode for using as a
     * default in methods that select a node from a node list.
     *
     * @return The empty node.
     */
    public static ConfigurationNode getEmptyNode() {
        return new ConfigurationNode(new HashMap<String, Object>());
    }

    @SuppressWarnings("unchecked")
    private void read(Object input) throws ConfigurationException {
        try {
            if (null == input) {
                this.root = new HashMap<>();
            } else {
                this.root = (Map<String, Object>) input;
            }
        } catch (ClassCastException e) {
            throw new ConfigurationException("Root document must be an key-value structure");
        }
    }

    /**
     * Return the set header.
     *
     * @return The header comment.
     */
    public String getHeader() {
        return this.header;
    }

    /**
     * Set the header for the file as a series of lines that are terminated
     * by a new line sequence.
     *
     * @param headerLines header lines to prepend
     */
    public void setHeader(String... headerLines) {
        StringBuilder header = new StringBuilder();

        for (String line : headerLines) {
            if (header.length() > 0) {
                header.append("\r\n");
            }
            header.append(line);
        }

        setHeader(header.toString());
    }

    /**
     * Loads the configuration file. All errors are thrown away.
     */
    public void load() {
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(this.file);
            read(this.yaml.load(new UnicodeReader(stream)));
        } catch (final IOException | ConfigurationException e) {
            this.root = new HashMap<>();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (final IOException ignored) {}
        }
    }

    /**
     * Saves the configuration to disk. All errors are clobbered.
     *
     * @return true if it was successful
     */
    public boolean save() {
        FileOutputStream stream = null;

        File parent = file.getParentFile();

        if (parent != null) {
            parent.mkdirs();
        }

        try {
            stream = new FileOutputStream(this.file);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            if (this.header != null) {
                writer.append(this.header);
                writer.append("\r\n");
            }
            this.yaml.dump(this.root, writer);
            return true;
        } catch (final IOException ignored) {
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (final IOException ignored) {
            }
        }

        return false;
    }

    /**
     * Set the header for the file. A header can be provided to prepend the
     * YAML data output on configuration save. The header is
     * printed raw and so must be manually commented if used. A new line will
     * be appended after the header, however, if a header is provided.
     *
     * @param header header to prepend
     */
    public void setHeader(String header) {
        this.header = header;
    }
}

class EmptyNullRepresenter extends Representer {

    public EmptyNullRepresenter() {
        super(new DumperOptions());
        this.nullRepresenter = new EmptyRepresentNull();
    }

    // Code borrowed from snakeyaml (http://code.google.com/p/snakeyaml/source/browse/src/test/java/org/yaml/snakeyaml/issues/issue60/SkipBeanTest.java)
    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
        NodeTuple tuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        Node valueNode = tuple.getValueNode();
        if (valueNode instanceof CollectionNode) {
            // Removed null check
            if (Tag.SEQ.equals(valueNode.getTag())) {
                SequenceNode seq = (SequenceNode) valueNode;
                if (seq.getValue().isEmpty()) {
                    return null; // skip empty lists
                }
            }
            if (Tag.MAP.equals(valueNode.getTag())) {
                //noinspection ConstantConditions
                MappingNode seq = (MappingNode) valueNode;
                if (seq.getValue().isEmpty()) {
                    return null; // skip empty maps
                }
            }
        }
        return tuple;
    }

    protected class EmptyRepresentNull implements Represent {

        public Node representData(Object data) {
            return representScalar(Tag.NULL, ""); // Changed "null" to "" so as to avoid writing nulls
        }
    }
    // End of borrowed code
}
