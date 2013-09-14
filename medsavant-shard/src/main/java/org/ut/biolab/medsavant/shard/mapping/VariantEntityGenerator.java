/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.shard.mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import jodd.io.FileUtil;
import jodd.util.ClassLoaderUtil;

import org.ut.biolab.medsavant.shard.file.FileUtils;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;

/**
 * Generator of variant entities.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantEntityGenerator implements EntityGenerator {
    private static final String PACK_BEG = "package ";
    private static final String PACK_END = ";\n";
    private static final String PACK = "org.ut.biolab.medsavant.shard.variant";
    private static final String IMPORT = "import java.io.Serializable;\n";
    private static final String CLASS_START = "public class ";
    private static final String CLASS_END = "}";
    private static final String IMPL = " implements Serializable {\n";
    private static final String HASHCODE = "@Override\n public int hashCode() {\n final int prime = 31;\n int result = 1;\n result = prime * result + ((variant_id == null) ? 0 : variant_id.hashCode());\n return result;\n }\n";
    private static final String TO_STRING = "@Override\n public String toString() {\n return \"\" + variant_id;\n }";
    private static final String CLASS_NAME_BASE = "DynamicVariant";

    private String className = CLASS_NAME_BASE;
    private String pack = PACK;
    private String construct = "public " + className + "() { super(); }\n";
    private String eq = "@Override\n public boolean equals(Object obj) {\n if (this == obj)\n return true;\n if (obj == null)\n return false;\n if (getClass() != obj.getClass())\n return false;\n "
            + className
            + " other = ("
            + className
            + ") obj;\n if (variant_id == null) {\n if (other.variant_id != null)\n return false;\n } else if (!variant_id.equals(other.variant_id))\n return false;\n return true;\n }\n";
    private List<ClassField> fields;
    private List<String> getters;
    private List<String> setters;
    private Random generator;

    private static VariantEntityGenerator instance = null;
    private Class<?> clazz = null;

    private void addBasicFields() {
        // fields.add(new ClassField("private static final", "long",
        // "serialVersionUID", "1L"));
        fields.add(new ClassField("private", "Integer", "position", "0"));
        fields.add(new ClassField("private", "Integer", "upload_id", "0"));
        fields.add(new ClassField("private", "Integer", "file_id", "0"));
        fields.add(new ClassField("private", "Integer", "variant_id", "0"));
        fields.add(new ClassField("private", "String", "dna_id", "\"\""));
        fields.add(new ClassField("private", "String", "chrom", "\"\""));
        fields.add(new ClassField("private", "String", "dbsnp_id", "\"\""));
        fields.add(new ClassField("private", "String", "ref", "\"\""));
        fields.add(new ClassField("private", "String", "alt", "\"\""));
        fields.add(new ClassField("private", "Float", "qual", "new Float(0)"));
        fields.add(new ClassField("private", "String", "filter", "\"\""));
        fields.add(new ClassField("private", "String", "variant_type", "\"\""));
        fields.add(new ClassField("private", "String", "zygosity", "\"\""));
        fields.add(new ClassField("private", "String", "gt", "\"\""));
        fields.add(new ClassField("private", "String", "custom_info", "\"\""));
    }

    private void addBasicGettersAndSetters() {
        getters.add("public Integer getUpload_id() {\nreturn upload_id;\n}\n");
        setters.add("public void setUpload_id(Integer upload_id) {\nthis.upload_id = upload_id;\n}\n");
        getters.add("public Integer getFile_id() {\nreturn file_id;\n}\n");
        setters.add("public void setFile_id(Integer file_id) {\nthis.file_id = file_id;\n}\n");
        getters.add("public Integer getVariant_id() {\nreturn variant_id;\n}\n");
        setters.add("public void setVariant_id(Integer variant_id) {\nthis.variant_id = variant_id;\n}\n");
        getters.add("public String getDna_id() {\nreturn dna_id;\n}\n");
        setters.add("public void setDna_id(String dna_id) {\nthis.dna_id = dna_id;\n}\n");
        getters.add("public String getChrom() {\nreturn chrom;\n}\n");
        setters.add("public void setChrom(String chrom) {\nthis.chrom = chrom;\n}\n");
        getters.add("public Integer getPosition() {\nreturn position;\n}\n");
        setters.add("public void setPosition(Integer position) {\nthis.position = position;\n}\n");
        getters.add("public String getDbsnp_id() {\nreturn dbsnp_id;\n}\n");
        setters.add("public void setDbsnp_id(String dbsnp_id) {\nthis.dbsnp_id = dbsnp_id;\n}\n");
        getters.add("public String getRef() {\nreturn ref;\n}\n");
        setters.add("public void setRef(String ref) {\nthis.ref = ref;\n}\n");
        getters.add("public String getAlt() {\nreturn alt;\n}\n");
        setters.add("public void setAlt(String alt) {\nthis.alt = alt;\n}\n");
        getters.add("public Float getQual() {\nreturn qual;\n}\n");
        setters.add("public void setQual(Float qual) {\nthis.qual = qual;\n}\n");
        getters.add("public String getFilter() {\nreturn filter;\n}\n");
        setters.add("public void setFilter(String filter) {\nthis.filter = filter;\n}\n");
        getters.add("public String getVariant_type() {\nreturn variant_type;\n}\n");
        setters.add("public void setVariant_type(String variant_type) {\nthis.variant_type = variant_type;\n}\n");
        getters.add("public String getZygosity() {\nreturn zygosity;\n}\n");
        setters.add("public void setZygosity(String zygosity) {\nthis.zygosity = zygosity;\n}\n");
        getters.add("public String getGt() {\nreturn gt;\n}\n");
        setters.add("public void setGt(String gt) {\nthis.gt = gt;\n}\n");
        getters.add("public String getCustom_info() {\nreturn custom_info;\n}\n");
        setters.add("public void setCustom_info(String custom_info) {\nthis.custom_info = custom_info;\n}\n");
    }

    protected VariantEntityGenerator() {
        generator = new Random();

        fields = new ArrayList<ClassField>();
        addBasicFields();

        getters = new ArrayList<String>();
        setters = new ArrayList<String>();
        addBasicGettersAndSetters();
    }

    public static VariantEntityGenerator getInstance() {
        if (instance == null) {
            instance = new VariantEntityGenerator();
        }

        return instance;
    }

    @Override
    public String getSource() {
        StringBuffer s = new StringBuffer();
        s.append(PACK_BEG);
        s.append(pack);
        s.append(PACK_END);
        s.append(IMPORT);
        s.append(CLASS_START);
        s.append(className);
        s.append(IMPL);

        for (ClassField f : fields) {
            s.append(f.toString());
            s.append("\n");
        }
        for (String t : getters) {
            s.append(t);
        }
        for (String t : setters) {
            s.append(t);
        }

        s.append(construct);
        s.append(eq);
        s.append(HASHCODE);
        s.append(TO_STRING);
        s.append(CLASS_END);

        return s.toString();
    }

    @Override
    public Class<?> getCompiled() {
        return clazz;
    }

    @Override
    public void compile() {
        try {
            // generate a random class name - we need to do this because you
            // can't reload a class on a classloader without garbage collecting
            // the classloader itself, which we cannot do, because it's being
            // used by Hibernate
            setClassName(CLASS_NAME_BASE + Math.abs(generator.nextInt()));

            // save source in .java file.
            File root = DirectorySettings.getTmpDirectory();
            File sourceFile = new File(root, pack.replace(".", "/") + "/" + className + ".java");
            sourceFile.getParentFile().mkdirs();

            BufferedWriter bw = new BufferedWriter(new FileWriter(sourceFile, false));
            bw.write(getSource());
            bw.close();

            // compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, sourceFile.getPath());
            File compiledFile = new File(root, pack.replace(".", "/") + "/" + className + ".class");

            // load the class - this is a classloader hack and pretty much the
            // only way this can be accomplished, do not touch
            String classPath = compiledFile.getAbsolutePath();
            byte[] classBytes;
            classBytes = FileUtil.readBytes(classPath);
            clazz = ClassLoaderUtil.defineClass(pack + "." + className, classBytes);

            // clean up
            FileUtils.deleteFile(sourceFile.getPath());
            FileUtils.deleteFile(compiledFile.getPath());
        } catch (IOException e) {
            System.err.println("Failed to load the variant class.");
        }

    }

    private void setClassName(String className) {
        this.className = className;
        construct = "public " + className + "() { super(); }\n";
        eq = "@Override\n public boolean equals(Object obj) {\n if (this == obj)\n return true;\n if (obj == null)\n return false;\n if (getClass() != obj.getClass())\n return false;\n "
                + className
                + " other = ("
                + className
                + ") obj;\n if (variant_id == null) {\n if (other.variant_id != null)\n return false;\n } else if (!variant_id.equals(other.variant_id))\n return false;\n return true;\n }\n";
    }

    public String getClassName() {
        return className;
    }

    public String getPackage() {
        return pack;
    }

    public List<ClassField> getFields() {
        return fields;
    }

    public void setFields(List<ClassField> fields) {
        this.fields = fields;

        getters = new ArrayList<String>();
        setters = new ArrayList<String>();
        for (ClassField f : fields) {
            getters.add(getGetter(f));
            setters.add(getSetter(f));
        }
    }

    private String getGetter(ClassField field) {
        return "public " + field.getType() + " get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1) + "() {\nreturn " + field.getName() + ";\n}\n";
    }

    private String getSetter(ClassField field) {
        return "public void set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1) + "(" + field.getType() + " " + field.getName() + ") {\nthis."
                + field.getName() + " = " + field.getName() + ";\n}\n";
    }

    @Override
    public void addField(ClassField field) {
        fields.add(field);
        getters.add(getGetter(field));
        setters.add(getSetter(field));
    }

}
