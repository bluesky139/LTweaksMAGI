package li.lingfeng.compiler;

import com.google.auto.service.AutoService;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.xml.parsers.DocumentBuilderFactory;

import li.lingfeng.lib.AppLoad;

@AutoService(javax.annotation.processing.Processor.class)
@SupportedAnnotationTypes({
        "li.lingfeng.lib.AppLoad"
})
public class Processor extends AbstractProcessor {

    private Messager mMessager;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void generateLoader(TypeElement methodsTypeElement, RoundEnvironment env) throws Exception {
        JavaFileObject genFile = processingEnv.getFiler().createSourceFile("li.lingfeng.magi.L");
        String genFilePath = genFile.toUri().toString();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "Processor is generating " + genFilePath);
        Writer writer = genFile.openWriter();
        mAppPath = genFilePath.substring(0, genFilePath.lastIndexOf("/build/generated/ap_generated_sources/"));
        mMessager.printMessage(Diagnostic.Kind.NOTE, "Processor mAppPath = " + mAppPath);

        HashMap<String, List<String>> modules = new HashMap();
        HashMap<String, String> keyToPackages = new HashMap();
        for (Element element_ : env.getElementsAnnotatedWith(methodsTypeElement)) {
            TypeElement element = (TypeElement) element_;
            AppLoad load = element.getAnnotation(AppLoad.class);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "Processor " + element.getSimpleName());
            List<String> names = modules.get(load.packageName());
            if (names == null) {
                names = new ArrayList();
                modules.put(load.packageName(), names);
            }
            names.add(element.getQualifiedName().toString());
            keyToPackages.put(load.pref(), load.packageName());
        }

        writer.write("package li.lingfeng.magi;\n\n");
        writer.write("import li.lingfeng.magi.tweaks.TweakBase;\n\n");
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
            writer.write("            case \"" + kv.getKey() + "\":\n");
            writer.write("                return \"" + kv.getValue() + "\";\n");
        }
        writer.write("        }\n");
        writer.write("        return null;\n");
        writer.write("    }\n");

        writer.write("}");
        writer.close();
    }
}