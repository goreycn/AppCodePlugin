package dialog;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class CreateJsonModelDlg extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea1;
    private JTextField tfPrefix;
    private JTextArea taJsonSample;
    private JTextField tfBaseClassName;

    // 记录每一层的Entity
    private HashMap<String, ArrayList<String>> entityMap = new HashMap<>();
    // 生成的排序键, 因为生成 类的时候, 要逆序生成
    private ArrayList<String> entityKeys = new ArrayList<>();
    // 要生成协议的数据
    private ArrayList<String> protocolKeys = new ArrayList<>();


    public CreateJsonModelDlg() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {

        // add your code here
        String _prefix = this.tfPrefix.getText();
        String _json = this.taJsonSample.getText();
        String _baseClass = this.tfBaseClassName.getText();

        JSONObject rootJson = new JSONObject(_json);

        entityMap.clear();
        entityKeys.clear();
        protocolKeys.clear();

        ClassNamePrefix = _prefix;
        createWith(ClassNamePrefix, rootJson);

        System.out.print(entityMap);
        if (myIf != null){
            myIf.myMethod(_prefix, entityKeys, entityMap, protocolKeys, _baseClass);
        }
        dispose();
    }

    String ClassNamePrefix = "";

    /**
     * 可以递归使用
     *
     * @param _prefix
     * @param rootJson
     */
    void createWith(String _prefix, JSONObject rootJson){


        ArrayList<String> propList = getPropListByPrefix(_prefix);

        for (String key : rootJson.keySet()) {

            String _key = key.substring(0,1).toUpperCase() + key.substring(1);

            // 值类型
            Object obj = rootJson.get(key);
            String valueType = obj.getClass().getSimpleName();

            if (valueType.equals("String")){
                propList.add("@property (nonatomic, strong) NSString *" + key + ";");
            }
            else if (valueType.equals("Integer")){
                propList.add("@property (nonatomic, assign) NSInteger " + key + ";");
            }
            else if (valueType.equals("Boolean")){
                propList.add("@property (nonatomic, assign) BOOL " + key + ";");
            }
            else if (valueType.equals("Double")){
                propList.add("@property (nonatomic, assign) CGFloat " + key + ";");
            }
            else if (valueType.equals("JSONObject")){
                propList.add("@property (nonatomic, strong) " + ClassNamePrefix + _key + "*" + key + ";");
                createWith(key, (JSONObject)obj);
            }
            else if (valueType.equals("JSONArray")){

                JSONArray objArr = (JSONArray)obj;

                // 这里假定, 不存在 数组下面直接使用数组的场合
                if (objArr.length() > 0) {

                    propList.add("@property (nonatomic, strong) NSArray<" + ClassNamePrefix + _key + "> *" + key + ";");
                    protocolKeys.add(_key);

                    createWith(key, (JSONObject)objArr.get(0));
                }
                else {
                    propList.add("@property (nonatomic, strong) NSArray *" + key + ";");
                }

            }
        }
    }

    /**
     * 从全局字典里拿到对应Key的属性列表
     * @param _prefix
     * @return
     */
    ArrayList<String> getPropListByPrefix(String _prefix) {
        if (entityMap.containsKey(_prefix)) {
            return entityMap.get(_prefix);
        }
        else {
            ArrayList<String> list = new ArrayList<>();
            entityKeys.add(_prefix);
            entityMap.put(_prefix, list);
            return list;
        }
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        CreateJsonModelDlg dialog = new CreateJsonModelDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public interface MyIf {
        public void myMethod(String className, ArrayList keyList, HashMap entityMap, ArrayList protocolList, String baseClassName);
    }

    MyIf myIf;

    public MyIf getMyIf() {
        return myIf;
    }

    public void setMyIf(MyIf myIf) {
        this.myIf = myIf;
    }

}
