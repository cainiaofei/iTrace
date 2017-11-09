package cn.edu.nju.cs.itrace4.parser;

import cn.edu.nju.cs.itrace4.io._;
import cn.edu.nju.cs.itrace4.parser.javatext.JField;
import cn.edu.nju.cs.itrace4.parser.javatext.JMethod;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by niejia on 15/2/10.
 */
public class JavaTextParser {

    private String outputDir;

    private final CompilationUnit root;
    private TypeDeclaration typeDec;
    /*
    Struct elements in a java file
     */
    private String className;
    private String classDoc;
    private List<JMethod> jMethodList;
    private List<JField> jFieldList;

    private String fileName;

    // For convenient export parsed file
    private Map<String, String> methodNameContentMap;
    private String classContent;

    public JavaTextParser(String path) {

        String javaSource = _.readFile(path);
        File file = new File(path);
        fileName = file.getName();
        this.outputDir = file.getParentFile().getParent();

        ASTParser parsert = ASTParser.newParser(AST.JLS3);
        parsert.setSource(javaSource.toCharArray());
        this.root = (CompilationUnit) parsert.createAST(null);

        // types represent all class in this file, includes public and non-public class
        List types = root.types();

        if (!types.isEmpty()) {
            // types.get(0) is the first class in this file, in most case is the public class
            this.typeDec = (TypeDeclaration) types.get(0);
        } else {
//            _.abort("No Class exists in this java file");

            //System.out.println(("No Class exists in this java file"));
            return;
        }

        className = typeDec.getName().toString();

        if (typeDec.getJavadoc() != null) {
            classDoc = typeDec.getJavadoc().toString();
        } else {
            classDoc = "";
        }
    }

    private void parseClass() {
//        System.out.printf("Parsing %s...\n", className);

        jFieldList = new ArrayList<>();

        if (typeDec == null) {
            return;
        }

        for (FieldDeclaration field : typeDec.getFields()) {
            String type = field.getType().toString();

            for (Object fragment : field.fragments()) {
                JField jf = new JField();
                jf.setTypeName(field.getType().toString());

                String fieldName = fragment.toString();
                if (fieldName.endsWith("=null")) {
                    fieldName = fieldName.split("=null")[0];
                }
                jf.setFieldName(fieldName);
                jFieldList.add(jf);
            }
        }

//        System.out.printf("%d fields in %s.\n", jFieldList.size(), className);

        parseMethod();

        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append(" ");

        for (JField field : jFieldList) {
            sb.append(field.getFieldName());
            sb.append(" ");
        }

        if (jMethodList != null) {
            for (JMethod method : jMethodList) {
                sb.append(method.getMethodName());
                sb.append(" ");
                for (String p : method.getParaNameList()) {
                    sb.append(p);
                    sb.append(" ");
                }
            }
        }


        sb.append("\n");

        if (jMethodList != null) {
            for (JMethod method : jMethodList) {
                sb.append(method.getDoc());
                sb.append(" ");
            }
        }


        sb.append(classDoc);
        sb.append("\n");

//        System.out.println(sb);
        classContent = sb.toString();
    }

    private void parseMethod() {

        methodNameContentMap = new LinkedHashMap<>();
        // is this check required ?
        PackageDeclaration packetDec = root.getPackage();
        if (packetDec == null) {
            return;
//            _.abort("PackageDeclaration is null");
        }

        jMethodList = new ArrayList<>();

        if (typeDec == null) return;

        for (MethodDeclaration method : typeDec.getMethods()) {
            JMethod jm = new JMethod();
            jm.setClassName(className);
            jm.setMethodName(method.getName().toString());
            Javadoc doc = method.getJavadoc();
            // if the method doc doesn't exist
            if (doc != null) {
                jm.setDoc(doc.toString());
            } else {
                jm.setDoc("");
            }

            for (Object obj : method.parameters()) {
                SimpleName paraName = ((SingleVariableDeclaration) obj).getName();
                jm.addParaName(paraName.toString());
            }
//            System.out.println(jm);
            methodNameContentMap.put(className + "#" + jm.getMethodName(), jm.toString());
            jMethodList.add(jm);
        }

//        System.out.printf("%d methods in %s.\n", jMethodList.size(), className);
    }


    public void exportParsedMethod() {
        parseMethod();

        if (methodNameContentMap == null) _.abort("Parsing method failed");

        File methodDir = new File(outputDir + "/method/code");
        methodDir.mkdirs();

        for (String f : methodNameContentMap.keySet()) {
            String filePath = methodDir + "/" + f + ".txt";
            _.writeFile(methodNameContentMap.get(f), filePath);
        }

    }

    public void exportParsedClass() {

        parseClass();

        if (classContent == null) {
            return;
//            _.abort("Class is not parsed.");
        }
        File classDir = new File(outputDir + "/class/code");
        classDir.mkdirs();


        String filePath = classDir.getPath() + "/" + className + ".txt";
//        String filePath = classDir.getPath() + "/" + fileName.split(".java")[0] + ".txt";
//        System.out.println(" filePath = " + filePath );
        _.writeFile(classContent.toString(), filePath);
    }
}


