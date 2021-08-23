package org.ivance.handler;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import org.ivance.annotation.CommandHandler;
import org.ivance.annotation.HandlerSingleton;
import org.ivance.annotation.PrefixedHandler;
import org.ivance.python.PythonSandbox;
import org.ivance.python.PythonScriptUnsafeException;

import java.io.File;
import java.util.Random;

@HandlerSingleton
@SuppressWarnings("unused")
public class PythonScriptHandler {
    private final PythonSandbox sandbox = new PythonSandbox();
    private final Random random = new Random();
    private int counter = 0;

    @PrefixedHandler(prefix = {"!python", "!py", "!exec", "#python", "# python"})
    public void exec(Contact contact, User sender, String script) {
        try {
            String result = sandbox.exec(script).trim();
            if (result.contains("\n")) {
                contact.sendMessage("Out[" + counter + "]:\n" + result);
            } else {
                contact.sendMessage("Out[" + counter + "]: "  + result);
            }
        } catch (PythonScriptUnsafeException exception) {
            contact.sendMessage(exception.getMessage());
        }
    }

    @PrefixedHandler(prefix = {"!calculate", "!calc", "!print"})
    public void calculate(Contact contact, User sender, String expression) {
        try {
            if (expression.contains("\n") || expression.contains("\r")) {
                contact.sendMessage("Illegal expression");
            }
            String resultIdentifier = "calc_result_" + random.nextInt();
            sandbox.exec(resultIdentifier + " = " + expression);
            if (sandbox.getLastExecuteCondition() == PythonSandbox.ExecuteCondition.SUCCEED) {
                contact.sendMessage(sandbox.getPythonObject(resultIdentifier).toString());
            } else {
                contact.sendMessage("Illegal expression: " + expression);
            }
        } catch (PythonScriptUnsafeException exception) {
            contact.sendMessage(exception.getMessage());
        }
    }

    @CommandHandler(command = {"!reset", "!clear"})
    public void reset(Contact contact, User sender, String[] arguments) {
        counter = 0;
        sandbox.reset();
    }

    // TODO: Add Permission System
    @CommandHandler(command = "!addpacks")
    public void addSitePackages(Contact contact, User sender, String[] arguments) {
        String path = arguments[1];
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            sandbox.addExternalSitePackages(arguments[1]);
            contact.sendMessage("Successfully added site-packages directory");
        } else {
            contact.sendMessage("Directory \"" + path + "\" does not exist");
        }
    }

    @CommandHandler(command = "!execcond")
    public void printLastCondition(Contact contact, User sender, String[] arguments) {
        switch (sandbox.getLastExecuteCondition()) {
            case DEFAULT:
                contact.sendMessage("ExecuteCondition: Default");
                break;
            case SUCCEED:
                contact.sendMessage("ExecuteCondition: Succeed");
                break;
            case FAIL:
                contact.sendMessage("ExecuteCondition: Fail");
                break;
            case BLOCKED:
                contact.sendMessage("ExecuteCondition: Blocked");
                break;
        }
    }

    @CommandHandler(command = "!autoclear")
    public void setAutoClear(Contact contact, User sender, String[] arguments) {
        if (arguments[1].equals("on") || arguments[1].equals("true")) {
            sandbox.setAutoClearOutput(true);
            contact.sendMessage("AutoClear: On");
        } else if (arguments[1].equals("off") || arguments[1].equals("false")) {
            sandbox.setAutoClearOutput(true);
            contact.sendMessage("AutoClear: Off");
        }
    }

    @CommandHandler(command = "!clearbuffer")
    public void clearBuffer(Contact contact, User sender, String[] arguments) {
        sandbox.clearBuffer();
    }
}
