import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.awt.RelativePoint;
import dialog.CreateJsonModelDlg;
import javafx.application.Application;
import org.apache.velocity.runtime.directive.Foreach;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by goreyjp on 15/9/29.
 */
public class CreateJsonModel extends AnAction implements CreateJsonModelDlg.MyIf {

    private AnActionEvent _event;

    @Override
    public void actionPerformed(AnActionEvent event) {
        _event = event;

        CreateJsonModelDlg dialog = new CreateJsonModelDlg();
        dialog.pack();
        dialog.setMyIf(this);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    @Override
    public void myMethod(final String classNamePrefix, final ArrayList keyList, final HashMap entityMap, final ArrayList protocolList, final String baseClassName) {
        Project project = _event.getProject();
        final Editor editor = _event.getData(PlatformDataKeys.EDITOR);
        final Document doc = _event.getRequiredData(CommonDataKeys.EDITOR).getDocument();



        Runnable r = new Runnable() {
            @Override
            public void run() {

                // 清空现有文本
                int lastOffset = doc.getLineEndOffset(doc.getLineCount() - 1);
                doc.deleteString(0, lastOffset);

                // 写入头文件
                doc.insertString(0, "#import <Foundation/Foundation.h>\n#import \"BaseModel.h\"\n\n");

                // 光标移到文件尾
                lastOffset = doc.getLineEndOffset(doc.getLineCount() - 1);
                editor.getCaretModel().moveToOffset(lastOffset);

                // entity 数量
                int entityCount = keyList.size();

                StringBuffer sbImplement = new StringBuffer();

                for (int i = 0; i < entityCount; i++) {

                    StringBuffer sb = new StringBuffer();

                    String key = (String) keyList.get(i);

                    // 顺序序遍历每一个entity, 但是由于下面的 doc.insertString方法, 每次都是在当前的光标处插入, 所以,在那里完成逆序
                    ArrayList<String> propList = (ArrayList<String>) entityMap.get(key);

                    if (key.equals(classNamePrefix)) {
                        key = "";
                    }
                    else {
                        key = key.substring(0,1).toUpperCase() + key.substring(1);
                    }

                    sb.append("@interface " + classNamePrefix + key + " : " + baseClassName + "\n");
                    for (Object str : propList) {
                        sb.append(str + "\n");
                    }

                    sb.append("@end\n");
                    sbImplement.append("@implementation " + classNamePrefix + key + "\n");
                    sbImplement.append("@end\n\n");

                    int offset = editor.getCaretModel().getOffset();
                    doc.insertString(offset, sb.toString());

                }

                // 把implement 单独写在文件最后
                lastOffset = doc.getLineEndOffset(doc.getLineCount() - 1);
                doc.insertString(lastOffset, sbImplement.toString());


                // 最后把协议头生成一下
                StringBuffer sb = new StringBuffer();

                int protocolCount = protocolList.size();
                for (int i = 0; i < protocolCount; i++) {
                    String protocolName = classNamePrefix + protocolList.get(i);
                    sb.append("@protocol " + protocolName + " @end\n");
                }
                sb.append("\n");
                int offset = editor.getCaretModel().getOffset();
                doc.insertString(offset, sb.toString());
            }
        };
        WriteCommandAction.runWriteCommandAction(project, r);
    }
}
