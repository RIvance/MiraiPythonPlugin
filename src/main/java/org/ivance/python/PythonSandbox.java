package org.ivance.python;

import org.python.core.PyString;
import org.python.core.PyUnicode;
import org.python.util.PythonInterpreter;

import java.io.ByteArrayOutputStream;

public class PythonSandbox {

    private PythonInterpreter interpreter = new PythonInterpreter();

    private boolean autoClearOutput = true;
    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

    private String[] bannedKeywords;
    private String initScriptTemplate;

    private final String[] defaultBannedKeywords = {
        "os", "sys", "pickle", "commands", "subprocess",    // modules
        "eval", "exec", "open", "input", "compile", "dir",  // functions
        "__builtins__", "__import__",
    };

    private final String defaultInitScriptTemplate = (
        "banned_keywords = [${banned_keywords}]             \n" +
        "for keyword in banned_keywords:                    \n" +
        "    if __builtins__.__dict__.get(keyword) != None: \n" +
        "        del __builtins__.__dict__[keyword]         \n"
    );

    enum ExecuteCondition {
        DEFAULT, SUCCEED, FAIL
    }

    private ExecuteCondition executeCondition = ExecuteCondition.DEFAULT;

    private void init() {
        StringBuilder bannedKeywordsBuilder = new StringBuilder();
        for (String keyword : defaultBannedKeywords) {
            bannedKeywordsBuilder.append("'").append(keyword).append("' ,");
        }

        String initScript = initScriptTemplate.replace("${banned_keywords}", bannedKeywordsBuilder.toString());
        interpreter.exec(initScript);

        interpreter.setOut(outputBuffer);
        interpreter.setErr(outputBuffer);
    }

    public PythonSandbox() {
        this.bannedKeywords = defaultBannedKeywords;
        this.initScriptTemplate = defaultInitScriptTemplate;
        init();
    }

    public PythonSandbox(boolean allowSystemPermissions) {
        if (!allowSystemPermissions) {
            this.bannedKeywords = defaultBannedKeywords;
            this.initScriptTemplate = defaultInitScriptTemplate;
            init();
        }
    }

    public PythonSandbox(String initScriptTemplate) {
        this.bannedKeywords = defaultBannedKeywords;
        this.initScriptTemplate = defaultInitScriptTemplate + initScriptTemplate;
        init();
    }

    public PythonSandbox(String ... bannedKeywords) {
        this.bannedKeywords = bannedKeywords;
        this.initScriptTemplate = defaultInitScriptTemplate;
        init();
    }

    public void reset() {
        interpreter.cleanup();
        interpreter.close();
        interpreter = new PythonInterpreter();
        init();
    }

    public void setAutoClearOutput(boolean doAutoClear) {
        this.autoClearOutput = doAutoClear;
    }

    public void clearBuffer() {
        outputBuffer.reset();
    }

    public String getBufferedOutput() {
        return outputBuffer.toString();
    }

    public ExecuteCondition getLastExecuteCondition() {
        return this.executeCondition;
    }

    public String exec(String rawScript) throws PythonScriptUnsafeException {
        for (String keyword : bannedKeywords) {
            String patten = "(([\\s\\S]*\\W)|[\\W]*)" + keyword + "([\\W]*|(\\W[\\s\\S]*))";
            if (rawScript.matches(patten)) {
                throw new PythonScriptUnsafeException("Script contains banned keyword \"" + keyword + "\"");
            }
        }

        PyString script = new PyUnicode(rawScript);
        try {
            interpreter.exec(script);
            this.executeCondition = ExecuteCondition.SUCCEED;
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            this.executeCondition = ExecuteCondition.FAIL;
        }

        String result = outputBuffer.toString();
        if (autoClearOutput) {
            clearBuffer();
        }
        return result;
    }
}
