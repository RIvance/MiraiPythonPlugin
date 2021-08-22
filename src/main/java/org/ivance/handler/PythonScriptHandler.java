package org.ivance.handler;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import org.ivance.annotation.CommandHandler;
import org.ivance.annotation.HandlerSingleton;
import org.ivance.annotation.PrefixedHandler;
import org.ivance.python.PythonSandbox;
import org.ivance.python.PythonScriptUnsafeException;

@HandlerSingleton
@SuppressWarnings("unused")
public class PythonScriptHandler {
    private final PythonSandbox sandbox = new PythonSandbox();
    private int counter = 0;

    @PrefixedHandler(prefix = {"!py", "!python", "!exec", "#python", "# python"})
    public void exec(Contact contact, User sender, String script) {
        try {
            String result = sandbox.exec(script).trim();
            contact.sendMessage("Out[" + counter + "]: " + result);
        } catch (PythonScriptUnsafeException exception) {
            contact.sendMessage(exception.getMessage());
        }
    }

    @CommandHandler(command = {"!reset", "!clear"})
    public void reset(Contact contact, User sender, String[] arguments) {
        counter = 0;
        sandbox.reset();
    }

    @CommandHandler(command = "!addpacks")
    public void addSitePackages(Contact contact, User sender, String[] arguments) {
        sandbox.addExternalSitePackages(arguments[1]);
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
