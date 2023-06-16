package li.lingfeng.compiler;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.google.auto.service.AutoService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.lib.HookMethod;

@AutoService(javax.annotation.processing.Processor.class)
@SupportedAnnotationTypes({
        "li.lingfeng.lib.AppLoad"
})
public class Processor extends AbstractProcessor {

    private Messager mMessager;
    private String mProjectRoot;
    private String mAppPath;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "Processor got annotations size " + annotations.size());
        if (annotations.size() != 1) {
            return false;
        }
        try {
            Iterator<TypeElement> iterator = (Iterator<TypeElement>) annotations.iterator();
            TypeElement methodsTypeElement = iterator.next();
            generateLoader(methodsTypeElement, env);
            generateHooker(env);
        } catch (Exception e) {
            log(e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                log(" at " + element);
            }
            throw new RuntimeException(e);
        }
        return true;
    }

    private void generateLoader(TypeElement methodsTypeElement, RoundEnvironment env) throws Exception {
        JavaFileObject genFile = processingEnv.getFiler().createSourceFile("li.lingfeng.magi.L");
        String genFilePath = genFile.toUri().toString();
        if (genFilePath.startsWith("file://")) {
            genFilePath = genFilePath.substring("file://".length());
        }
        log("Generating " + genFilePath);
        Writer writer = genFile.openWriter();
        mAppPath = genFilePath.substring(0, genFilePath.lastIndexOf("/build/generated/ap_generated_sources/"));
        log("mAppPath = " + mAppPath);
        mProjectRoot = genFilePath.substring(0, genFilePath.lastIndexOf("/app/build/generated/ap_generated_sources/"));
        log("mProjectRoot = " + mProjectRoot);

        HashMap<String, List<String>> modules = new HashMap();
        HashMap<String, String> keyToPackages = new HashMap();
        for (Element element_ : env.getElementsAnnotatedWith(methodsTypeElement)) {
            TypeElement element = (TypeElement) element_;
            AppLoad load = element.getAnnotation(AppLoad.class);
            log(" " + element.getSimpleName());
            List<String> names = modules.get(load.packageName());
            if (names == null) {
                names = new ArrayList();
                modules.put(load.packageName(), names);
            }
            names.add(element.getQualifiedName().toString());
            keyToPackages.put(load.pref(), load.packageName());
        }

        writer.write("package li.lingfeng.magi;\n\n");
        writer.write("import li.lingfeng.magi.tweaks.base.TweakBase;\n\n");
        writer.write("public class L {\n\n");

        writer.write("    public static TweakBase[] instantiateTweaks(String niceName) {\n");
        writer.write("        TweakBase[] tweaks = null;\n");
        writer.write("        switch (niceName) {\n");
        for (Map.Entry<String, List<String>> kv : modules.entrySet()) {
            writer.write("            case \"" + kv.getKey() + "\":\n");
            List<String> names = kv.getValue();
            writer.write("                tweaks = new TweakBase[" + names.size() + "];\n");
            for (int i = 0; i < names.size(); ++i) {
                writer.write("                tweaks[" + i + "] = new " + names.get(i) + "();\n");
            }
            writer.write("                break;\n");
        }
        writer.write("        }\n");
        writer.write("        return tweaks;\n");
        writer.write("    }\n\n");

        writer.write("    public static String keyToPackage(String key) {\n");
        writer.write("        switch (key) {\n");
        for (Map.Entry<String, String> kv : keyToPackages.entrySet()) {
            if (kv.getKey() == null || kv.getKey().isEmpty()) {
                continue;
            }
            writer.write("            case \"" + kv.getKey() + "\":\n");
            writer.write("                return \"" + kv.getValue() + "\";\n");
        }
        writer.write("        }\n");
        writer.write("        return null;\n");
        writer.write("    }\n");

        writer.write("}");
        writer.close();
    }

    private void generateHooker(RoundEnvironment env) throws Exception {
        log("Generating hooker.");
        String path = mProjectRoot + "/app/src/main/java/li/lingfeng/magi/tweaks/base/IMethodBase.java";
        ParseResult<CompilationUnit> result = new JavaParser().parse(new File(path));
        if (!result.isSuccessful()) {
            throw new Exception("Can't parse " + path);
        }

        CompilationUnit unit = result.getResult().get();
        ClassOrInterfaceDeclaration cls = unit.getClassByName("IMethodBase").get();
        for (MethodDeclaration method : cls.getMethods()) {
            Optional<AnnotationExpr> opt = method.getAnnotationByClass(HookMethod.class);
            if (opt.isPresent()) {
                NormalAnnotationExpr annotation = (NormalAnnotationExpr) opt.get();
                String hooker = StringUtils.capitalize(method.getNameAsString());
                boolean isStatic = false;
                String returnType = null;
                for (MemberValuePair p : annotation.getPairs()) {
                    switch (p.getNameAsString()) {
                        case "isStatic":
                            BooleanLiteralExpr expr = p.getValue().asBooleanLiteralExpr();
                            if (expr != null) {
                                isStatic = expr.getValue();
                            }
                            break;
                        case "returnType":
                            returnType = p.getValue().asClassExpr().getTypeAsString();
                            break;
                    }
                }
                if (isStatic) {
                    throw new Exception("static hook method is not handled.");
                }

                JavaFileObject genFile = processingEnv.getFiler().createSourceFile(
                        "li.lingfeng.magi.tweaks.hooker." + hooker);
                String genFilePath = genFile.toUri().toString();
                log("Generating " + genFilePath);
                log(" hooker " + hooker + ", isStatic " + isStatic);
                Writer writer = genFile.openWriter();

                String template = FileUtils.readFileToString(new File(mProjectRoot
                        + "/compiler/src/main/java/li/lingfeng/compiler/Hooker.java.template"));
                StringBuilder builder = new StringBuilder();
                NodeList<ImportDeclaration> imports = unit.getImports();
                for (ImportDeclaration importDeclaration : imports) {
                    builder.append(importDeclaration.toString());
                }
                template = template.replace("###IMPORTS###", builder.toString());
                template = template.replace("###CLASS_NAME###", hooker);

                builder = new StringBuilder();
                NodeList<Parameter> parameters = method.getParameters();
                for (int i = 0; i < parameters.size(); ++i) {
                    Parameter parameter = parameters.get(i);
                    if (i > 0) {
                        builder.append(", ");
                    }
                    builder.append(parameter.getTypeAsString());
                    builder.append(" ");
                    builder.append(parameter.getNameAsString());
                }
                template = template.replace("###THIS_OBJECT_AND_ARGS_WITH_TYPE###", builder.toString());
                template = template.replace("###IMPL_METHOD_NAME###", method.getNameAsString());


                builder = new StringBuilder();
                for (int i = 1; i < parameters.size(); ++i) {
                    Parameter parameter = parameters.get(i);
                    if (i > 1) {
                        builder.append(", ");
                    }
                    builder.append(parameter.getNameAsString());
                }
                template = template.replace("###ARGS###", builder.toString());
                template = template.replace("###THIS_OBJECT###", "thisObject");
                template = template.replace("###COMMA_MAY_GONE###", parameters.size() == 1 ? "" : ",");

                template = template.replace("###RETURN_TYPE###",
                        returnType != null ? returnType : "void");
                template = template.replace("###RESULT_GET_RESULT###",
                        returnType != null ? "(" + returnType + ") result.getResult()" : "");
                template = template.replace("###SET_RESULT_SILENTLY###",
                        returnType != null ? "result.setResultSilently(ret);" : "");

                template = template.replace("###DECLARE_ORIGINAL_RET###",
                        returnType != null ? returnType + " ret;" : "");
                template = template.replace("###ASSIGN_TO_ORIGINAL_RET###",
                        returnType != null ? "ret = (" + returnType + ") " : "");
                template = template.replace("###RETURN_ORIGINAL_RET###",
                        returnType != null ? "return ret;" : "");

                writer.write(template);
                writer.close();
            }
        }
    }

    private void log(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "[Processor] " + msg);
    }
}