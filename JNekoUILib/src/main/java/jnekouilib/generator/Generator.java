package jnekouilib.generator;

import jnekouilib.appmenu.AppMenuGroup;
import jnekouilib.appmenu.AppMenuItem;
import jnekouilib.editor.Editor;
import jnekouilib.fragment.Fragment;
import jnekouilib.panel.PanelButton;
import jnekouilib.windows.UIDialog;

import javafx.application.Application;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Generator extends Application {
    private final UIDialog
            uWin = new UIDialog(900, 600, true, true, "Class generator");
    
    private final Fragment
            inTextF = new Fragment(),
            outTextF = new Fragment(),
            helpTextF = new Fragment();
    
    private final TextArea
            inText = new TextArea(),
            outText = new TextArea();
    
    private final StringBuilder 
            classHeader = new StringBuilder(),
            getSetBlock = new StringBuilder(),
            variableBlock = new StringBuilder();
    
    
    private boolean isTypeNamePresent(String[] array, String type) {
        if (array == null) return false;
        if (array.length < 3) return false;
        return (type.trim().equalsIgnoreCase(array[0].trim()));
            
    }
    
    private String _uC(String s) {
        if (s.length() == 1) return s.toUpperCase();
        return (s.substring(0, 1).toUpperCase() + s.substring(1));
    }
    
    private void generateAnnotationGet(String varName, String varType, GeneratorFieldType gft) {
        switch(gft) {
            case Boolean:
                getSetBlock
                        .append("\t@UIBooleanField(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.GETTER, readOnly=0, labelText=\"!# Enter text here\")\n");
                break;
            case StringSingleline:
                getSetBlock
                        .append("\t@UIStringField(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.GETTER, readOnly=0, maxChars=255, helpText=\"Enter text here\", labelText=\"!# Enter text here\")\n");
                break;
            case StringMultiline:
                getSetBlock
                        .append("\t@UITextArea(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.GETTER, readOnly=0, maxChars=4096, helpText=\"Enter text here\", labelText=\"!# Enter text here\")\n");
                break;
            case Long:
                getSetBlock
                        .append("\t@UILongField(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.GETTER, readOnly=0, labelText=\"!# Enter text here\")\n");
                break;
            case CollectionSingleselect:
                getSetBlock
                        .append("\t@UICollection(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.GETTER, multiSelect=0, yesNoBoxPresent=1, text=\"!# Enter text here\")\n");
                break;
            case CollectionMultiselect:
                getSetBlock
                        .append("\t@UICollection(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.GETTER, multiSelect=1, yesNoBoxPresent=1, text=\"!# Enter text here\")\n");
                break;
        }

    }
    
    private void generateAnnotationSet(String varName, String varType, GeneratorFieldType gft) {
        switch(gft) {
            case Boolean:
                getSetBlock
                        .append("\t@UIBooleanField(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.SETTER)\n");
                break;
            case StringSingleline:
                getSetBlock
                        .append("\t@UIStringField(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.SETTER)\n");
                break;
            case StringMultiline:
                getSetBlock
                        .append("\t@UITextArea(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.SETTER)\n");
                break;
            case Long:
                getSetBlock
                        .append("\t@UILongField(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.SETTER)\n");
                break;
            case CollectionSingleselect:
                getSetBlock
                        .append("\t@UICollection(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.SETTER)\n");
                break;
            case CollectionMultiselect:
                getSetBlock
                        .append("\t@UICollection(name=\"")
                        .append(varName)
                        .append("\", type=UIFieldType.SETTER)\n");
                break;
        }
    }
    
    private void generateGetSetBlock(String varName, String varType, String listParam, int sortIndex, GeneratorFieldType gft) {
        if (listParam.trim().length() >= 2) {
            if ((gft == GeneratorFieldType.StringSingleline) || (gft == GeneratorFieldType.StringMultiline)) {
                if (varType.equalsIgnoreCase("lh1")) getSetBlock.append("\n\t@UIListItemHeader\n");
                if (varType.equalsIgnoreCase("lh2")) getSetBlock.append("\n\t@UIListItemSubtitle\n");
                if (varType.equalsIgnoreCase("lh3")) getSetBlock.append("\n\t@UIListItemTextLine\n");
                if (varType.equalsIgnoreCase("lrb")) getSetBlock.append("\n\t@UIListItemRightText\n");
            } else 
                getSetBlock.append("// ERROR: can't generate list headers for non-string fields!\n");
        } 

        getSetBlock.append("\n\t@UISortIndex(index=").append(sortIndex).append(")\n");

        generateAnnotationGet(varName, varType, gft);
        getSetBlock
                .append("\tpublic ")
                .append(varType)
                .append(" get")
                .append(_uC(varName))
                .append("() {\n\t\treturn this.")
                .append(varName)
                .append(";\n\t}\n\n");
        
        generateAnnotationSet(varName, varType, gft);
        getSetBlock
                .append("\tpublic void set")
                .append(_uC(varName))
                .append("(")
                .append(varType)
                .append(" ")
                .append(varName)
                .append(") {\n\t\tthis.") 
                .append(varName)
                .append(" = ")
                .append(varName)
                .append(";\n\t}\n");
    }
    
    private void compile() {
        outText.setText("");
        classHeader.delete(0, classHeader.length());
        getSetBlock.delete(0, getSetBlock.length());
        variableBlock.delete(0, variableBlock.length());
        
        final String s = inText.getText().replace('\r', ' ').trim();
        final String[] arr = s.split("\n");
        if (arr == null) {
            outText.setText("Error: incorrect input");
            return;
        }

        String className = "defaultClassName";
        getSetBlock
                .append("\t@UISortIndex(index=-100)\n\t@UILongField(name=\"DBID\", type=UIFieldType.GETTER, readOnly=1, labelText=\"DB ID\")\n")
                .append("\tpublic long getID() {\n\t\treturn ID;\n\t}\n\n")
                .append("\t@UILongField(name=\"DBID\", type=UIFieldType.SETTER)\n")
                .append("\tpublic void setID(long ID) {\n\t\tthis.ID = ID;\n\t}\n");
        
        for (int i=0; i<arr.length; i++) {
            final String[] arr2 = arr[i].split(";");
            if (arr2 != null) {
                if (isTypeNamePresent(arr2, "#class")) {
                    className = arr2[2];
                }
                
                if (isTypeNamePresent(arr2, "#bool")) {
                    variableBlock
                            .append("\n\t@Column(name=\"").append(arr2[2]).append("\", unique = false)\n")
                            .append("\tprivate boolean ").append(arr2[2]).append(";\n"); 
                    generateGetSetBlock(arr2[2], "boolean", arr2[1], i, GeneratorFieldType.Boolean);
                }
                
                if (isTypeNamePresent(arr2, "#long")) {
                    variableBlock
                            .append("\n\t@Column(name=\"").append(arr2[2]).append("\", unique = false)\n")
                            .append("\tprivate long ").append(arr2[2]).append(";\n"); 
                    generateGetSetBlock(arr2[2], "long", arr2[1], i, GeneratorFieldType.Long);
                }
                
                if (isTypeNamePresent(arr2, "#str")) {
                    variableBlock
                            .append("\n\t@Column(name=\"").append(arr2[2]).append("\", unique = false, nullable = true, length = 255)\n")
                            .append("\tprivate String ").append(arr2[2]).append(";\n"); 
                    generateGetSetBlock(arr2[2], "String", arr2[1], i, GeneratorFieldType.StringSingleline);
                }
                
                if (isTypeNamePresent(arr2, "#text")) {
                    variableBlock
                            .append("\n\t@Column(name=\"").append(arr2[2]).append("\", unique = false, nullable = true, length = 255)\n")
                            .append("\tprivate String ").append(arr2[2]).append(";\n"); 
                    generateGetSetBlock(arr2[2], "String", arr2[1], i, GeneratorFieldType.StringMultiline);
                }
                
                if (isTypeNamePresent(arr2, "#ssl")) {
                    variableBlock.append("\tprivate ArrayList ").append(arr2[2]).append(";\n"); 
                    generateGetSetBlock(arr2[2], "ArrayList", arr2[1], i, GeneratorFieldType.CollectionSingleselect);
                }
                
                if (isTypeNamePresent(arr2, "#msl")) {
                    variableBlock.append("\tprivate ArrayList ").append(arr2[2]).append(";\n"); 
                    generateGetSetBlock(arr2[2], "ArrayList", arr2[1], i, GeneratorFieldType.CollectionMultiselect);
                }
            }
        }
        
        classHeader
                .append("import com.jneko.jnekouilib.anno.*;\nimport java.io.Serializable;\nimport javax.persistence.*;\n\n")
                .append("@Entity\n@UIListItem\n@UILibDataSource\npublic class ")
                .append(_uC(className))
                .append(" implements Serializable {\n")
                .append("\t@Id\n\t@GeneratedValue(strategy=GenerationType.AUTO)\n\t@Column(name=\"ID\", unique = true, nullable = false)\n\tprivate long ID;\n");

        outText.setText(classHeader.toString() + variableBlock.toString() + "\n" + getSetBlock.toString() + "}\n");
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        uWin.addMenu(
                new AppMenuGroup(
                        "Source data", "menuHeaderBlack", "menuHeaderIcon",
                        new AppMenuItem("Fields code", (c) -> {
                            uWin.showFragment(inTextF, true);
                        }).defaultSelected(),
                        new AppMenuItem("Help", (c) -> {
                            uWin.showFragment(helpTextF, true);
                        })
                )
        );
        
        inTextF.getChildren().add(inText);
        inTextF.getPanel().addSeparator();
        inTextF.getPanel().addNode(
                new PanelButton("iconCompileProfile", "Create class...", e -> {
                    compile();
                    uWin.showFragment(outTextF, false);
                })
        );
        
        outTextF.getChildren().add(outText);
        outTextF.getPanel().addSeparator();
        outTextF.getPanel().addNode(
                new PanelButton("iconBackForList", "Back", e -> {
                    outTextF.back();
                })
        );
        
        inText.setWrapText(true);
        inText.getStyleClass().addAll("eStringFieldElementTextArea", Editor.maxWidthStyle, "maxHeight");
        
        outText.setWrapText(true);
        outText.getStyleClass().addAll("eStringFieldElementTextArea", Editor.maxWidthStyle, "maxHeight");
        
        uWin.showFragment(inTextF, true);
        uWin.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
