import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.jetbrains.cidr.lang.autoImport.OCAutoImportHelper;
import com.jetbrains.cidr.lang.psi.*;
import com.jetbrains.cidr.lang.quickfixes.OCImportSymbolFix;
import com.jetbrains.cidr.lang.util.OCElementFactory;

import java.util.List;

/**
 * Created by goreyjp on 15/10/28.
 */
public class BaseCellAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getProject();
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        final Document doc = event.getRequiredData(CommonDataKeys.EDITOR).getDocument();

        VirtualFile currentFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        String path = currentFile.getCanonicalPath();
        String mPath = path.substring(0, path.length() - 1) + "m";

        OCClassDeclaration cd = PsiUtility.getOCClassDeclaration(event);
        final List<OCProperty> listProperty = cd.getProperties();

        OCInterface ocInterface = PsiUtility.findInterfaceInElement(cd.getParent(), cd.getName());
        final OCImplementation ocImpl = PsiUtility.getImplementationFor(cd);

        final List<OCMethod> methods = ocImpl.getMethods();

        Runnable r = new Runnable() {
            @Override
            public void run() {

                // 代码格式化
                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(ocImpl.getProject());
                ;
                // 添加 约束生成方法
                String strUpdateConstrains = generateUpdateConstrainsMethodBy(listProperty);
                OCMethod methodUpdateConstrains = OCElementFactory.methodFromText(strUpdateConstrains, ocImpl, true);

                OCMethod firstMethod = ocImpl.getMethods().get(0);

                codeStyleManager.reformat(ocImpl.addAfter(methodUpdateConstrains, firstMethod));

                // 添加初化方法
                String strInit = generateAddSubviewMethodBy(listProperty);
                OCMethod methodInit = OCElementFactory.methodFromText(strInit, ocImpl, true);
                codeStyleManager.reformat(ocImpl.addAfter(methodInit, firstMethod));

                // 添加Get方法
                for (OCProperty p : listProperty) {
                    // 属性类型名
                    String typeName = p.getDeclaration().getType().getName();
                    // 属性变量名
                    String varName = p.getDeclaration().getDeclarators().get(0).getName();

                    OCMethod getMethod = PsiUtility.createGetMethodWith(typeName, varName, ocImpl);
                    codeStyleManager.reformat(ocImpl.addAfter(getMethod, ocImpl.getMethods().get(ocImpl.getMethods().size() - 1)));

                    // 按钮的话, 要自动增加响应方法
                    if (typeName.equals("UIButton")) {
                        OCMethod actionMethod = PsiUtility.createActionClickMethodWith(typeName, varName, ocImpl);
                        codeStyleManager.reformat(ocImpl.addAfter(actionMethod, ocImpl.getMethods().get(ocImpl.getMethods().size() - 1)));
                    }
                }

            }

        };
        WriteCommandAction.runWriteCommandAction(project, r);
    }


    /**
     * 生成约束布局的模板方法
     *
     * @param listProperty
     * @return
     */
    String generateUpdateConstrainsMethodBy(List<OCProperty> listProperty) {

        StringBuffer sb = new StringBuffer();
        sb.append("- (void)updateConstraints {\n" +
                "UIView *sv = self.contentView;\n");

        for (OCProperty p : listProperty) {
            String str = createConstrainsTemplateStringBy(p);
            sb.append(str);
        }

        sb.append("    [super updateConstraints];\n}\n");
        return sb.toString();
    }

    /**
     * 生成AddSubview
     *
     * @param listProperty
     * @return
     */
    String generateAddSubviewMethodBy(List<OCProperty> listProperty) {

        StringBuffer sb = new StringBuffer();
        sb.append("- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {\n" +
                "    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];" +
                "    if (self) {");

        for (OCProperty p : listProperty) {
            // 属性变量名
            String varName = p.getDeclaration().getDeclarators().get(0).getName();
            String str = "[self.contentView addSubview:self." + varName + "];\n";
            sb.append(str);
        }

        sb.append(" [self updateConstraints];\n" +
                "    }\n" +
                "    return self;\n" +
                "}\n");
        return sb.toString();
    }

    private String createConstrainsTemplateStringBy(OCProperty p) {
        StringBuffer sb = new StringBuffer();

        OCDeclaration pDec = p.getDeclaration();

        // 属性类型名
        String typeName = p.getDeclaration().getType().getName();
        // 属性变量名
        String varName = p.getDeclaration().getDeclarators().get(0).getName();

        if (isUIType(typeName)) {
            sb.append("   [self." + varName + " mas_makeConstraints:^(MASConstraintMaker *m) {\n\n" + "}];\n\n");
        }
        return sb.toString();
    }

    /**
     * 是否是一个UI组件
     *
     * @param typeName
     * @return
     */
    private boolean isUIType(String typeName) {
        if (typeName.startsWith("UI")) {
            return true;
        } else {
            return false;
        }
    }


}