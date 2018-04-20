package jnekouilib.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import jnekouilib.anno.UIBooleanField;
import jnekouilib.anno.UICollection;
import jnekouilib.anno.UIEditableCollection;
import jnekouilib.anno.UIFieldType;
import jnekouilib.anno.UILibDataSource;
import jnekouilib.anno.UILongField;
import jnekouilib.anno.UISortIndex;
import jnekouilib.anno.UIStringField;
import jnekouilib.anno.UITextArea;
import jnekouilib.fragment.Fragment;
import jnekouilib.fragment.FragmentList;
import jnekouilib.utils.ReflectionUtils;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class Editor extends Fragment implements EditorFragmentListActionListener {
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(this.getClass());
    
    public static final String 
            okStyle         = "controlsGoodInput", 
            errorStyle      = "controlsBadInput",
            maxWidthStyle   = "maxWidth"
            ;
    
    private enum FieldType {
        BOOLEAN_CHECK, STRING, LONG, STRING_MULTILINE, LIST, EDITABLE_LIST
    }
    
    private class EditorMethods {
        public Method getter;
        public Method setter;
        public FieldType fieldType;
        public Parent uiElementRef;
        public Object ref;
        public String refName;
    }
    
    private final Map<String, Collection>
            collectionHelpers = new HashMap<>();
    
    private final Map<String, EditorMethods>
            methodsMap = new HashMap<>();
    
    private final Label
            title = new Label();
    
    private final ScrollPane
            elementsSP= new ScrollPane();
    
    private final VBox
            vContainer = new VBox();
    
    private void genUI() {       
        this.getStyleClass().addAll("maxHeight", "maxWidth");
        title.getStyleClass().addAll("StringFieldElementLabel", "maxWidth"); 

        elementsSP.getStyleClass().addAll("maxWidth", "maxHeight", "ScrollPane");
        elementsSP.setContent(vContainer);
        elementsSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        elementsSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        elementsSP.setFitToWidth(true);
        elementsSP.setFitToHeight(false);
    }
    
    public Editor() {
        super();
        genUI();
        
        super.getChildren().addAll(elementsSP);
    }
    
    public Editor(String eTitle) {
        super();
        genUI();
        
        if (eTitle != null) title.setText(eTitle);
        super.getChildren().addAll(title, elementsSP);
    }
    
    public Editor(Parent customHeader, Parent customFooter) {
        super();
        genUI();
        
        if (customHeader != null) super.getChildren().add(customHeader);
        super.getChildren().add(elementsSP);
        if (customFooter != null) super.getChildren().add(customFooter);
    }
    
    public void addCollectionHelper(String name, Collection collection) {
        collectionHelpers.put(name, collection);
    }
    
    public void validateForm(EditorFormValidator efv) {
        final Set<String> ml = methodsMap.keySet();
        ml.forEach(element -> {
            final EditorMethods em = methodsMap.get(element);
            if (em.uiElementRef instanceof EditorTypeValidable)
                ((EditorTypeValidable) em.uiElementRef).setValid(efv.validateForm(em.ref, em.uiElementRef));
        });
    }

    private void mmCreate(String mName, Object o, Method m, FieldType ft) {
        if (!methodsMap.containsKey(mName)) {
            final EditorMethods mm = new EditorMethods();
            mm.fieldType = ft;
            mm.ref = o;
            methodsMap.put(mName, mm);
        }
    }

    private void readObjectTextArea(Object o, Method m) {
        final String mName = m.getAnnotation(UITextArea.class).name();
        if (mName == null) return;
        mmCreate(mName, o, m, FieldType.STRING_MULTILINE);

        if (m.getAnnotation(UITextArea.class).type() == UIFieldType.SETTER) {
            methodsMap.get(mName).setter = m;
        } else if (m.getAnnotation(UITextArea.class).type() == UIFieldType.GETTER) {
            if (m.getReturnType().equals(String.class)) {
                methodsMap.get(mName).getter = m;

                final ElementTextArea elString = new ElementTextArea();
                elString.fillFromObject(o, m); 

                vContainer.getChildren().add(elString);
                methodsMap.get(mName).uiElementRef = elString;
            }
        }
    } 
    
    private void readObjectTextField(Object o, Method m) {
        final String mName = m.getAnnotation(UIStringField.class).name();
        if (mName == null) return;
        mmCreate(mName, o, m, FieldType.STRING);

        if (m.getAnnotation(UIStringField.class).type() == UIFieldType.SETTER) {
            methodsMap.get(mName).setter = m;
        } else if (m.getAnnotation(UIStringField.class).type() == UIFieldType.GETTER) {
            if (m.getReturnType().equals(String.class)) {
                methodsMap.get(mName).getter = m;

                final ElementTextField elString = new ElementTextField();
                elString.fillFromObject(o, m);

                vContainer.getChildren().add(elString);
                methodsMap.get(mName).uiElementRef = elString;
            }
        }
    }
    
    private void readObjectSimpleNumberField(Object o, Method m) {
        final String mName = m.getAnnotation(UILongField.class).name();
        if (mName == null) return;
        mmCreate(mName, o, m, FieldType.LONG);

        if (m.getAnnotation(UILongField.class).type() == UIFieldType.SETTER) {
            methodsMap.get(mName).setter = m;
        } else if (m.getAnnotation(UILongField.class).type() == UIFieldType.GETTER) {
            if (m.getReturnType().equals(long.class)) {
                methodsMap.get(mName).getter = m;

                final ElementSimpleNumberField elString = new ElementSimpleNumberField();
                elString.fillFromObject(o, m);

                vContainer.getChildren().add(elString);
                methodsMap.get(mName).uiElementRef = elString;
            }
        }
    }
    
    private void readObjectCheckBox(Object o, Method m) {
        final String mName = m.getAnnotation(UIBooleanField.class).name();
        if (mName == null) return;
        mmCreate(mName, o, m, FieldType.BOOLEAN_CHECK);

        if (m.getAnnotation(UIBooleanField.class).type() == UIFieldType.SETTER) {
            methodsMap.get(mName).setter = m;
        } else if (m.getAnnotation(UIBooleanField.class).type() == UIFieldType.GETTER) {
            if (m.getReturnType().equals(boolean.class)) {
                methodsMap.get(mName).getter = m;

                final ElementCheckBox elString = new ElementCheckBox();
                elString.fillFromObject(o, m);

                vContainer.getChildren().add(elString);
                methodsMap.get(mName).uiElementRef = elString;
            }
        }
    }

    @Override
    public void OnListNoClick(EditorFragmentList fl) {
        final Set<String> ml = methodsMap.keySet();
        ml.forEach(element -> {
            EditorMethods em = methodsMap.get(element);
            if ((em.getter == null) || (em.setter == null)) return;
            if (em.uiElementRef.equals(fl.getParentElement())) {
                saveCollection(em, element);
            }
        });
    }
    
    private void readObjectCollection(Object o, Method m) {
        final String mName = m.getAnnotation(UICollection.class).name();
        if (mName == null) return;
        mmCreate(mName, o, m, FieldType.LIST);

        if (m.getAnnotation(UICollection.class).type() == UIFieldType.SETTER) {
            methodsMap.get(mName).setter = m;
        } else if (m.getAnnotation(UICollection.class).type() == UIFieldType.GETTER) {
            if (Collection.class.isAssignableFrom(m.getReturnType())) { 
                methodsMap.get(mName).getter = m;

                final ElementListLink elString = new ElementListLink(mName, o, m);
                elString.create();
                 
                final EditorFragmentList flist = new EditorFragmentList(
                        m.getAnnotation(UICollection.class).multiSelect() == 1
                );
                elString.setParentFL(flist);
                flist.setParentElement(elString);
                
                elString.setXLabelText(m.getAnnotation(UICollection.class).text());
                try {
                    final Collection annoRetVal = (Collection) m.invoke(o);
                    flist.readCollection(annoRetVal, collectionHelpers.get(mName), this);
                    if (annoRetVal != null)
                        elString.setOnMouseClicked(value -> {
                            this.getHost().showFragment(flist, false);
                        });
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                }

                vContainer.getChildren().add(elString);
                methodsMap.get(mName).uiElementRef = elString;
            }
        }
    }
    
    private void readObjectEditableCollection(Object o, Method m) {
        final String mName = m.getAnnotation(UIEditableCollection.class).name();
        if (mName == null) return;
        mmCreate(mName, o, m, FieldType.EDITABLE_LIST);
        
        if (m.getAnnotation(UIEditableCollection.class).type() == UIFieldType.SETTER) {
            methodsMap.get(mName).setter = m;
        } else if (m.getAnnotation(UIEditableCollection.class).type() == UIFieldType.GETTER) {
            if (Collection.class.isAssignableFrom(m.getReturnType())) { 
                final Class cc = ReflectionUtils.getGenericOfCollection(o.getClass(), m);
                
                final FragmentList fle = new FragmentList(cc, mName, this);
                
                methodsMap.get(mName).getter = m;
                final String xtext  = m.getAnnotation(UIEditableCollection.class).text();
                final String xtitle = m.getAnnotation(UIEditableCollection.class).title();
                if ((title != null) && (xtext != null))
                    fle.setHeaderText(xtitle, xtext);
                
                try {
                    final Collection annoRetVal = (Collection) m.invoke(o);
                    //fle.addCollectionHelper(mName, annoRetVal); 
                    fle.setCollection(annoRetVal);
                    fle.create();

                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                vContainer.getChildren().add(fle);
            }
        }
    }
    
    private void createSeparator() {
        final VBox separator = new VBox();
        separator.setMaxSize(9999, 8);
        separator.setPrefSize(9999, 8);
        separator.setMinSize(8, 8);
        vContainer.getChildren().add(separator);
    }
    
    private void createSeparatorHeader(String name) {
        final Label sh = new Label(name);
        sh.getStyleClass().addAll("eStringFieldElementSeparator1", "maxWidth");
        vContainer.getChildren().add(sh);
    }

    public void readObject(Object obj) {
        if (obj == null) {
            logger.debug("readObject: object is null!");
            return;
        }
        
        vContainer.getChildren().clear();
        methodsMap.clear();
        
        final Class<?> cl = obj.getClass();
        if (!cl.isAnnotationPresent(UILibDataSource.class)) {
            logger.debug("readObject: object hasn't '@UILibDataSource' annotation! Object has a "+cl.getAnnotations().length+" annotations, "+Arrays.toString(cl.getAnnotations())+".");
            return;
        }
        
        final Method[] methods = cl.getMethods();
        if (methods == null) {
            logger.debug("readObject: can't read a class methods.");
            return;
        }
        
        if (methods.length == 0) {
            logger.debug("readObject: class is empty.");
            return;
        }
        
        final List<Method> mSorted = Arrays.asList(methods);
        mSorted.sort((a, b) -> {
               final Long 
                       valA = (a.isAnnotationPresent(UISortIndex.class)) ? a.getAnnotation(UISortIndex.class).index() : -9999,
                       valB = (b.isAnnotationPresent(UISortIndex.class)) ? b.getAnnotation(UISortIndex.class).index() : -9999;
               return valA.compareTo(valB);
        });
        
        for (int i=0; i<mSorted.size(); i++) {
            final Method m = mSorted.get(i);
            if (m.isAnnotationPresent(UISortIndex.class)) {
                if (m.getAnnotation(UISortIndex.class).separatorPresent() == 1) {
                    createSeparator();
                    if (m.getAnnotation(UISortIndex.class).separatorName().length() > 0) 
                        createSeparatorHeader(m.getAnnotation(UISortIndex.class).separatorName());
                }
            }

            if (m.isAnnotationPresent(UIStringField.class))     readObjectTextField(obj, m);
            if (m.isAnnotationPresent(UITextArea.class))        readObjectTextArea(obj, m);
            if (m.isAnnotationPresent(UILongField.class))       readObjectSimpleNumberField(obj, m);
            if (m.isAnnotationPresent(UIBooleanField.class))    readObjectCheckBox(obj, m);
            
            if (m.isAnnotationPresent(UICollection.class))              readObjectCollection(obj, m);
            if (m.isAnnotationPresent(UIEditableCollection.class))      readObjectEditableCollection(obj, m);
        }
        
        logger.debug("readObject: " + methodsMap.size() + " methods are mapped.");
    }
    
    private void saveStringField(EditorMethods em, String refName) {    
        if (! (em.uiElementRef instanceof ElementTextField)) return;
        
        final ElementTextField elString = (ElementTextField) em.uiElementRef;
        try {
            em.setter.invoke(em.ref, elString.getXText());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveTextArea(EditorMethods em, String refName) {
        if (! (em.uiElementRef instanceof ElementTextArea)) return;
        
        final ElementTextArea elString = (ElementTextArea) em.uiElementRef;
        try {
            em.setter.invoke(em.ref, elString.getXText());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveSimpleNumberField(EditorMethods em, String refName) {
        if (! (em.uiElementRef instanceof ElementSimpleNumberField)) return;
        
        final ElementSimpleNumberField elString = (ElementSimpleNumberField) em.uiElementRef;
        try {
            em.setter.invoke(em.ref, elString.getXNumber());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveCheckBox(EditorMethods em, String refName) {
        if (! (em.uiElementRef instanceof ElementCheckBox)) return;
        
        final ElementCheckBox elString = (ElementCheckBox) em.uiElementRef;
        try {
            em.setter.invoke(em.ref, elString.getValue());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveCollection(EditorMethods em, String refName) {
        if (! (em.uiElementRef instanceof ElementListLink)) return;
        
        final EditorFragmentList elList = ((ElementListLink) em.uiElementRef).getParentFL();
        if (Collection.class.isAssignableFrom(em.getter.getReturnType())) { 
            try {
                final Collection annoRetVal = (Collection) em.getter.invoke(em.ref);
                annoRetVal.clear();
                elList.saveCollection((name, obj) -> {
                    annoRetVal.add(obj);
                }, refName);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }
    
    public void saveObject() {
        final Set<String> ml = methodsMap.keySet();
        logger.debug("readObject: " + methodsMap.size() + " filelds ready to save.");
        
        ml.forEach(element -> {
            EditorMethods em = methodsMap.get(element);
            if ((em.getter == null) || (em.setter == null)) return;
            
            switch (em.fieldType) {
                case STRING:
                    saveStringField(em, element);
                    break;
                case STRING_MULTILINE:
                    saveTextArea(em, element);
                    break;
                case LONG:
                    saveSimpleNumberField(em, element);
                    break;
                case BOOLEAN_CHECK:
                    saveCheckBox(em, element);
                    break;   
                case LIST:
                    saveCollection(em, element);
                    break;
            }
        });
    }
}
