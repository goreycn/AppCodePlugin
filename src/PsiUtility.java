/**
 * Created by goreyjp on 15/10/28.
 */

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.cidr.lang.psi.*;
import com.jetbrains.cidr.lang.util.OCElementFactory;

import java.util.List;

public class PsiUtility {

    public static OCClassDeclaration getOCClassDeclaration(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAt, OCClassDeclaration.class);
    }

    public static OCInterface findInterfaceInElement(PsiElement element, String cName) {
        OCInterface ocx = null;
        for (OCInterface ocInterface : PsiTreeUtil.findChildrenOfType(element, OCInterface.class)) {
            String name = ocInterface.getName();

            if (cName.equals(name)) {
                ocx = ocInterface;
                break;
            }
        }

        return ocx;
    }

    public static OCInterface getInterfaceFor(OCClassDeclaration cls) {
        if (cls instanceof OCInterface) {
            return (OCInterface) cls;
        }

        OCFile contFile = cls.getContainingOCFile();
        if (contFile == null) {
            return null;
        }

        OCFile assocFile = contFile.getAssociatedFile();
        if (assocFile == null) {
            return null;
        }

        return findInterfaceInElement(assocFile, cls.getCanonicalName());
    }

    public static OCImplementation findImplementationInElement(PsiElement element, String cName) {
        OCImplementation ocx = null;
        for (OCImplementation ocImpl : PsiTreeUtil.findChildrenOfType(element, OCImplementation.class)) {
            String name = ocImpl.getName();

            if (cName.equals(name)) {
                ocx = ocImpl;
                break;
            }
        }

        return ocx;
    }

    public static OCImplementation getImplementationFor(OCClassDeclaration cls) {
        if (cls instanceof OCImplementation) {
            return (OCImplementation) cls;
        }

        OCFile contFile = cls.getContainingOCFile();
        if (contFile == null) {
            return null;
        }

        OCFile assocFile = contFile.getAssociatedFile();
        if (assocFile == null) {
            return null;
        }

        return findImplementationInElement(assocFile, cls.getCanonicalName());
    }

    public static OCMethod findImplementationMethodByName(OCImplementation impl, String methodName) {
        OCMethod result = null;

        List<OCMethod> methods = impl.getMethods();
        for (OCMethod method : methods) {
            if (method.getName().equals(methodName)) {
                result = method;
                break;
            }
        }

        return result;
    }

    public static OCMethod createGetMethodWith(String typeName, String varName, OCImplementation impl) {
        StringBuffer sb = new StringBuffer();
        sb.append("- (" + typeName + " *)" + varName + "{\n" +
                "    if (_" + varName + ") return _" + varName + ";");

        if ("UILabel".equals(typeName)) {
            sb.append("_" + varName + " = [UILabel labelWithTitle:@\"标题\" fontSize:APP_FONT_SIZE_M color:APP_COLOR_DARK_GRAY];\n");
        } else if ("UIView".equals(typeName)) {
            sb.append("_" + varName + " = [UIView viewWithColor:APP_COLOR_CLEAR frame:CGRectZero];\n");
        } else if ("UITextField".equals(typeName)) {
            sb.append("_" + varName + " = [UITextField textFiledWithText:@\"输入框\" placehold:@\"请输入\" fontSize:APP_FONT_SIZE_M];\n");
        } else if ("UIButton".equals(typeName)) {
            sb.append("_" + varName + " = [UIButton buttonWithTitle:@\"登录\" fontSize:APP_FONT_SIZE_M color:APP_COLOR_DARK_GRAY del:self sel:@selector(" + varName + "OnClicked:)];\n");
        } else if ("UIImageView".equals(typeName)) {
            sb.append("_" + varName + " = [UIImageView imageViewWithImage:[UIImage imageNamed:@\"\"]];\n");
        } else {
            sb.append("_" + varName + " = [[" + typeName + " alloc] init];");
        }
        sb.append("    return _" + varName + ";\n" +
                "}");

        OCMethod method = OCElementFactory.methodFromText(sb.toString(), impl, true);
        return method;
    }

    /**
     * 创建响应方法
     * @param typeName
     * @param varName
     * @param impl
     * @return
     */
    public static OCMethod createActionClickMethodWith(String typeName, String varName, OCImplementation impl) {
        StringBuffer sb = new StringBuffer();
        sb.append("- (void)btnNameOnClicked:(id)sender{\n" +
                "    \n" +
                "}\n");
        OCMethod method = OCElementFactory.methodFromText(sb.toString(), impl, true);
        return method;
    }
}
