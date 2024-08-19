/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WatchVarUT;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoManager;
import model.UTModel;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import service.CmnService;
import service.FileService;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//WatchVarUT//UtManager//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "UtManagerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@ActionID(category = "Window", id = "WatchVarUT.UtManagerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_UtManagerAction",
        preferredID = "UtManagerTopComponent"
)
@Messages({
    "CTL_UtManagerAction=UtManager",
    "CTL_UtManagerTopComponent=UT Manager",
    "HINT_UtManagerTopComponent=This is a UtManager window"
})
public final class UtManagerTopComponent extends TopComponent {

    // undo redo service
    private UndoManager undoManager = new UndoManager();

    // file to read and save
    private File file;



    // model
    UTModel model;

    public UtManagerTopComponent() {
        initComponents();
        setName(Bundle.CTL_UtManagerTopComponent());
        setToolTipText(Bundle.HINT_UtManagerTopComponent());

        // user define
        reset();
        setEvent();
        
        
        watchTextArea.disable();
        watchTextArea.setBackground(new java.awt.Color(240, 240, 240));
        addWatchesButton.disable();
    }

    // <editor-fold defaultstate="collapsed" desc="Set event for ALL">
    private void setEvent() {
        // event for watchTextArea
        watchTextArea.getDocument().addUndoableEditListener(undoManager);
        watchTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                watchTextAreaKeyPressed(evt);
            }
        });


        // event for open button
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleOpenFileButton(evt);
            }
        });

        // event for Save Button
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // Save Model to File
                saveToFile(file);
            }
        });

        // event for Save As Button
        saveAsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
//                file = null;
//                fileLabel.setText("file.ut");
                saveToFile(null);
            }
        });
        
        // event for add watch bubutton
        addWatchesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                SetWatches();
            }
        });


        // event for tree
        // left click
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                hanldeTreeItemChanged(tse);
            }
        });
        // right click
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                handleTreeShowMenu(evt);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Handle Tree Item Changed (Left Click Tree Items)">
    // item selected and previous
    private DefaultMutableTreeNode selectedNode;
    private DefaultMutableTreeNode previousNode;
    private DefaultMutableTreeNode parentSelectedNode;
    private DefaultMutableTreeNode previousParentSelectedNode;
    // ok
    public void hanldeTreeItemChanged(TreeSelectionEvent tse) {
        // (DefaultMutableTreeNode)
        previousNode = selectedNode;
        previousParentSelectedNode = parentSelectedNode;

        // luu vao model cai watch text area truoc
        if(previousNode != null) {
            String numStr = (String) previousNode.getUserObject();                  // #1 #2
            String parent = (String) previousParentSelectedNode.getUserObject();    // UT001 UT002
            model.getUts().get(parent).set(CmnService.strToNum(numStr) - 1, watchTextArea.getText());
        }

        TreePath path = tse.getNewLeadSelectionPath();
        if(path != null) {

            int depth = path.getPathCount();
            if(depth == 3) {
                watchTextArea.enable();
                watchTextArea.setBackground(Color.white);
                watchTextArea.requestFocus();
                
                selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                parentSelectedNode = (DefaultMutableTreeNode) selectedNode.getParent();

                Map<String, List<String>> mapUts = model.getUts();
                List<String> uts = mapUts.get(parentSelectedNode.getUserObject());

                String numStr = new StringBuilder((String) selectedNode.getUserObject()).delete(0, 1).toString();
                watchTextArea.setText(uts.get(Integer.valueOf(numStr) - 1));

                // display ut name
                String utName = (String) ((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject();
                utNameLabel.setText(utName + "#" + numStr);
            } else {
                selectedNode = null;
                previousNode = selectedNode;
                watchTextArea.disable();
                watchTextArea.setBackground(new java.awt.Color(240, 240, 240));
                addWatchesButton.disable();
            }
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set - handle event right click for Tree Item (Right Click Tree Items)">
    // register events for each level tree item
    public void handleTreeShowMenu(MouseEvent evt) {
        if(SwingUtilities.isRightMouseButton(evt)) {
            int row = tree.getRowForLocation(evt.getX(), evt.getY());
            TreePath path = tree.getPathForLocation(evt.getX(), evt.getY());

            if(row != -1 && path != null) {
                tree.setSelectionPath(path);

                int depth = path.getPathCount();
                if(depth == 1) {
                    showMenuOptionRoot(evt);
                } else if(depth == 2) {
                    showMenuOptionChild(evt);
                } else if(depth == 3) {
//                    showMenuOptionChildChild(evt);
                }
            } else {
                showMenuOptionRoot(evt);
            }
        }
    }


    // register menu for root level
    private void showMenuOptionRoot(MouseEvent evt) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1AddNewUt = new JMenuItem("Add new UT");
        JMenuItem item2RenameRoot = new JMenuItem("Rename Root");

        item1AddNewUt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
                String rootName = (String) rootNode.getUserObject();

                // enter new name
                String nameOfNextNode = (String) JOptionPane.showInputDialog("Enter UT Name (UT{XXX})");
                // neu bam cancel
                if(nameOfNextNode == null) {
                    return;
                }

                // check ten da ton tai chua
                if(model.getUts().containsKey(nameOfNextNode)) {
                    JOptionPane.showMessageDialog(null, "UT Name \"" + nameOfNextNode + "\" is existed", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // save to model
                model.getUts().putIfAbsent(nameOfNextNode, new ArrayList<String>(Arrays.asList("")));

                // insert new node
                DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode(nameOfNextNode);
                DefaultMutableTreeNode nextChildNode = new DefaultMutableTreeNode("#1");
                nextNode.add(nextChildNode);

                // get index to insert (theo thu tu tang dan String)
                int index = new ArrayList<String>(model.getUts().keySet()).indexOf(nameOfNextNode);
                ((DefaultTreeModel) tree.getModel()).insertNodeInto(nextNode, rootNode, index);

                // select added node
                TreePath newPath = new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(nextChildNode));
                tree.setSelectionPath(newPath);

            }
        });
        item2RenameRoot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // enter new name
                String rootName = (String) JOptionPane.showInputDialog("Enter Root Name");
                // neu bam cancel
                if(rootName == null) {
                    return;
                }

                // update model
                model.setUtName(rootName);

                // update tree
                DefaultMutableTreeNode rootElement = (DefaultMutableTreeNode) tree.getModel().getRoot();
                rootElement.setUserObject(rootName);
                ((DefaultTreeModel) tree.getModel()).nodeChanged(rootElement);
            }
        });

        menu.add(item1AddNewUt);
        menu.add(item2RenameRoot);
        menu.show(tree, evt.getPoint().x, evt.getPoint().y);
    }

    // register menu for 1st child level
    private void showMenuOptionChild(MouseEvent evt) {
        System.out.println("menu option Child");
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1AddNewStep = new JMenuItem("Add new Step");
        JMenuItem item2RenameUt = new JMenuItem("Rename UT");
        JMenuItem item3RemoveUT = new JMenuItem("Remove UT");
        JMenuItem item4RemoveLastStep = new JMenuItem("Remove Last Step");

        item1AddNewStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                String selectedNodeName = (String) selectedNode.getUserObject();

                // get total element of the current UT
                int count = model.getUts().get(selectedNodeName).size();

                // save to model a new child step node
                model.getUts().get(selectedNodeName).add("");

                // add new step node to tree
                DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode("#" + (count + 1));
                ((DefaultTreeModel) tree.getModel()).insertNodeInto(nextNode, selectedNode, selectedNode.getChildCount());

                // select added node
                TreePath newPath = new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(nextNode));
                tree.setSelectionPath(newPath);

            }
        });
        item2RenameUt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // enter new name
                String utName = (String) JOptionPane.showInputDialog("Enter UT Name (UT{XXX})");
                // neu bam cancel
                if(utName == null) {
                    return;
                }

                // get current node
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                String selectedNodeName = (String) selectedNode.getUserObject();

                // check ten da ton tai chua
                if(model.getUts().containsKey(utName)) {
                    JOptionPane.showMessageDialog(null, "UT Name \"" + utName + "\" is existed", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // update to model
                List<String> listStepOfOldUt = model.getUts().get(selectedNodeName);
                model.getUts().remove(selectedNodeName);
                model.getUts().put(utName, listStepOfOldUt);

                // update tree
                // remove current node
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) selectedNode.getParent();
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(selectedNode);

                // add new node to tree
                DefaultMutableTreeNode newUtNode = new DefaultMutableTreeNode(utName);
                for(int i = 1; i <= listStepOfOldUt.size(); i++) {
                    newUtNode.add(new DefaultMutableTreeNode("#" + i));
                }

                // get index to insert (theo thu tu tang dan String)
                int index = new ArrayList<String>(model.getUts().keySet()).indexOf(utName);
                ((DefaultTreeModel) tree.getModel()).insertNodeInto(newUtNode, rootNode, index);

            }
        });
        item3RemoveUT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // get current node
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                String selectedNodeName = (String) selectedNode.getUserObject();

                // show confirmation dialog
                int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove UT: \"" + selectedNodeName + "\"?\nAll steps will be removed!", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                    return;
                }

                // update to model
                List<String> listStepOfOldUt = model.getUts().get(selectedNodeName);
                model.getUts().remove(selectedNodeName);

                // update tree
                // remove current node
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) selectedNode.getParent();
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(selectedNode);

            }
        });
        item4RemoveLastStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                // get current node
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                String selectedNodeName = (String) selectedNode.getUserObject();

                // check size, if size == 1, ignore
                List<String> listSteps = model.getUts().get(selectedNodeName);
                if (listSteps.size() == 1) {
                    JOptionPane.showMessageDialog(null, "Cannot Remove!\nAn UT has to have at least 1 step", "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // show confirmation dialog
                int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove the last Step?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                    return;
                }

                // update to model
                List<String> listStepOfOldUt = model.getUts().get(selectedNodeName);
                int oldSize = listStepOfOldUt.size();
                listStepOfOldUt.remove(oldSize - 1);


                // update tree
                DefaultMutableTreeNode nodeToRemove = (DefaultMutableTreeNode) selectedNode.getChildAt(oldSize - 1);
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(nodeToRemove);

                // set previous node for node item change event
                previousNode = null;
            }
        });

        menu.add(item1AddNewStep);
        menu.add(item2RenameUt);
        menu.add(item3RemoveUT);
        menu.add(item4RemoveLastStep);
        menu.show(tree, evt.getPoint().x, evt.getPoint().y);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set event for Watch Text Area">
    // undo - redo - save - add watches key event
    private void watchTextAreaKeyPressed(java.awt.event.KeyEvent evt) {
        // int ctr_shift = KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK;
        int ctr = KeyEvent.CTRL_DOWN_MASK;
        try {
            // undo event
            if((evt.getModifiersEx() & ctr) == ctr && evt.getKeyCode() == KeyEvent.VK_Z) {
                undoManager.undo();

            // redo event
            } else if((evt.getModifiersEx() & ctr) == ctr && evt.getKeyCode() == KeyEvent.VK_Y) {
                undoManager.redo();

            // save event
            } else if((evt.getModifiersEx() & ctr) == ctr && evt.getKeyCode() == KeyEvent.VK_S) {
                // Save Model to File
                saveToFile(file);

            // add watches event
            } else if((evt.getModifiersEx() & ctr) == ctr && evt.getKeyCode() == KeyEvent.VK_ENTER) {
                // Set all content to watch
                SetWatches();
            }
        } catch(Exception e) {

        }
    }

    private void SetWatches() {
        String[] watches = watchTextArea.getText().split("\n");

        DebuggerManager debug = DebuggerManager.getDebuggerManager();
        debug.removeAllWatches();

        for(String watch : watches) {
            if(StringUtils.isNotBlank(watch)) {
                debug.createWatch(watch.trim());
            }
        }
    }

    private void saveToFile(File file) {
        // set current model by selecting the root node
        TreePath newPath = new TreePath((DefaultMutableTreeNode) tree.getModel().getRoot());
        tree.setSelectionPath(newPath);

        // save
        if(file == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("UT Files (*.ut)", "ut"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String fileName = fileChooser.getSelectedFile().getName();
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                String fileExtension = ".ut";
                if(!fileName.contains(fileExtension)) {
                    filePath += fileExtension;
                }

                this.file = new File(filePath);
                if (!this.file.exists()) {
                    try {
                        this.file.createNewFile();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                        System.out.println(e);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "file đã tồn tại", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                return;
            }
            fileLabel.setText(this.file.getName());
        }
        // save object Model to file
        FileService.writeObjectToFile(this.file, model);
    }
    // </editor-fold>


    // open file event
    public void handleOpenFileButton(java.awt.event.ActionEvent evt) {

        selectedNode = null;
        previousNode = null;
        parentSelectedNode = null;
        previousParentSelectedNode = null;
        model = null;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("UT Files (*.ut)", "ut"));
        int result = fileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            fileLabel.setText(fileChooser.getSelectedFile().getName());
            file = fileChooser.getSelectedFile();
        } else {
            return;
        }

        // TODO read the file
        model = FileService.readObjectFromFile(file);
        if (model == null) {
            initModel();

            // if exception
            if (!FileService.writeObjectToFile(file, model)) {
                JOptionPane.showMessageDialog(null, "Exception");
            }
        }

        // TODO set to tree
        model = FileService.readObjectFromFile(file);
        setTree();
        watchTextArea.setText("");
    }



    // set model to tree
    private void setTree() {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(model.getUtName());
        model.getUts().forEach(new BiConsumer<String, List<String>>() {
            public void accept(String t, List<String> u) {
                DefaultMutableTreeNode ut = new DefaultMutableTreeNode(t);
                for(int i = 0; i < u.size(); i++) {
                    ut.add(new DefaultMutableTreeNode("#" + (i + 1)));
                }
                root.add(ut);
            }
        });

        DefaultTreeModel modelTree = (DefaultTreeModel) tree.getModel();
        modelTree.setRoot(root);
        tree.setModel(modelTree);
    }

    // reset init when first load plugin
    private void reset() {
        jPanel1.setBackground(new java.awt.Color(240, 240, 240));
        jPanel2.setBackground(new java.awt.Color(240, 240, 240));
        jPanel3.setBackground(new java.awt.Color(240, 240, 240));
        jPanel4.setBackground(new java.awt.Color(240, 240, 240));
        jPanel5.setBackground(new java.awt.Color(240, 240, 240));
        jPanel6.setBackground(new java.awt.Color(240, 240, 240));
        jPanel7.setBackground(new java.awt.Color(240, 240, 240));
        jPanel8.setBackground(new java.awt.Color(240, 240, 240));
        jPanel9.setBackground(new java.awt.Color(240, 240, 240));

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // model init
        initModel();

        setTree();

    }

    // init the model
    private void initModel() {
        model = new UTModel();
        model.setUtName("ROOT");
        Map<String, List<String>> uts = new TreeMap<>();
        uts.put("UT001", new ArrayList<String>(Arrays.asList("")));
        model.setUts(uts);
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        fileLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        addWatchesButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        openFileButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        utNameLabel = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        watchTextArea = new javax.swing.JTextArea();

        jPanel1.setBackground(new java.awt.Color(204, 255, 255));

        jPanel4.setBackground(new java.awt.Color(204, 255, 204));

        fileLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(UtManagerTopComponent.class, "UtManagerTopComponent.fileLabel.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(204, 255, 255));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        tree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(tree);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
                .addGap(0, 7, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );

        jPanel3.setBackground(new java.awt.Color(255, 204, 204));

        jPanel8.setBackground(new java.awt.Color(204, 255, 204));

        addWatchesButton.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        addWatchesButton.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(addWatchesButton, org.openide.util.NbBundle.getMessage(UtManagerTopComponent.class, "UtManagerTopComponent.addWatchesButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(UtManagerTopComponent.class, "UtManagerTopComponent.saveButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(saveAsButton, org.openide.util.NbBundle.getMessage(UtManagerTopComponent.class, "UtManagerTopComponent.saveAsButton.text")); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addWatchesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
            .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(saveAsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(saveAsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addWatchesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel9.setBackground(new java.awt.Color(204, 255, 204));

        org.openide.awt.Mnemonics.setLocalizedText(openFileButton, org.openide.util.NbBundle.getMessage(UtManagerTopComponent.class, "UtManagerTopComponent.openFileButton.text")); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(openFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(openFileButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel5.setBackground(new java.awt.Color(204, 255, 204));

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));

        utNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(utNameLabel, org.openide.util.NbBundle.getMessage(UtManagerTopComponent.class, "UtManagerTopComponent.utNameLabel.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(utNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(utNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel7.setBackground(new java.awt.Color(204, 255, 204));

        watchTextArea.setColumns(20);
        watchTextArea.setRows(5);
        jScrollPane2.setViewportView(watchTextArea);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 741, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addWatchesButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton openFileButton;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JTree tree;
    private javax.swing.JLabel utNameLabel;
    private javax.swing.JTextArea watchTextArea;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
