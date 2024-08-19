/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WatchVarUT;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoManager;
import model.UTModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;


//        DefaultMutableTreeNode ut1 = new DefaultMutableTreeNode("UT001");
//        ut1.add(new DefaultMutableTreeNode("#1"));
//        ut1.add(new DefaultMutableTreeNode("#2"));
//        ut1.add(new DefaultMutableTreeNode("#3"));
//        DefaultMutableTreeNode ut2 = new DefaultMutableTreeNode("UT002");
//        ut2.add(new DefaultMutableTreeNode("#1"));
//        ut2.add(new DefaultMutableTreeNode("#2"));
//        ut2.add(new DefaultMutableTreeNode("#3"));
//        ut2.add(new DefaultMutableTreeNode("#4"));
//        root.add(ut1);
//        root.add(ut2);










/**
 * Top component which displays something.
 */
//@ConvertAsProperties(
//        dtd = "-//WatchVarUT//Test//EN",
//        autostore = false
//)
//@TopComponent.Description(
//        preferredID = "TestTopComponent",
//        //iconBase="SET/PATH/TO/ICON/HERE", 
//        persistenceType = TopComponent.PERSISTENCE_ALWAYS
//)
//@TopComponent.Registration(mode = "output", openAtStartup = true)
//@ActionID(category = "Window", id = "WatchVarUT.TestTopComponent")
//@ActionReference(path = "Menu/Window" /*, position = 333 */)
//@TopComponent.OpenActionRegistration(
//        displayName = "#CTL_TestAction",
//        preferredID = "TestTopComponent"
//)
//@Messages({
//    "CTL_TestAction=Test",
//    "CTL_TestTopComponent=Test Window",
//    "HINT_TestTopComponent=This is a Test window"
//})
public final class TestTopComponent extends TopComponent {

    private UTModel uTModel = new UTModel();
    
    private UndoManager undoManager = new UndoManager();
    public TestTopComponent() {
        initComponents();
//        setName(Bundle.CTL_TestTopComponent());
//        setToolTipText(Bundle.HINT_TestTopComponent());

        watchTextArea.getDocument().addUndoableEditListener(undoManager);
        
        uTModel.setUtName("KHIN_SZTR_B02");
        Map<String, List<String>> mapUts = uTModel.getUts();
        List<String> utt1 = new LinkedList<>();
        utt1.add("xx \nyy \nzz");
        utt1.add("a");
        utt1.add("b");
        mapUts.put("UT001", utt1);
        
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(uTModel.getUtName());
        uTModel.getUts().forEach(new BiConsumer<String, List<String>>() {
            @Override
            public void accept(String t, List<String> u) {
                DefaultMutableTreeNode ut1 = new DefaultMutableTreeNode(t);
                for(int i = 0; i < u.size(); i++) {
                    ut1.add(new DefaultMutableTreeNode("#" + (i + 1)));
                }
                root.add(ut1);
            }
        });
        
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(root);
        tree.setModel(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        
        // chi quan tam den child nho nhat
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                
                TreePath path = tse.getNewLeadSelectionPath();
                if(path != null) {
                    
                    int depth = path.getPathCount();
                    if(depth == 1) {
                    } else if(depth == 2) {
                    } else if(depth == 3) {
                        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                        
                        DefaultMutableTreeNode parentSelectedNode = (DefaultMutableTreeNode) selectedNode.getParent();
                        System.out.println(parentSelectedNode.getUserObject());
                        
                        Map<String, List<String>> mapUts = uTModel.getUts();
                        List<String> uts = mapUts.get(parentSelectedNode.getUserObject());
                        
                        String numStr = new StringBuilder((String) selectedNode.getUserObject()).delete(0, 1).toString();
                        watchTextArea.setText(uts.get(Integer.valueOf(numStr) - 1));
                    }
                }
            }
        });
        
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
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
                            showMenuOptionChildChild(evt);
                        }
                    } else {
                        showMenuOptionRoot(evt);
                    }
                }
            }
        });
    }
    
    private void showMenuOptionRoot(MouseEvent evt) {
        System.out.println("menu option Root");
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Add new UT");
        item1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // do sth
                System.out.println("add new UT");
            }
        });
        menu.add(item1);
        menu.show(tree, evt.getPoint().x, evt.getPoint().y);
    }
    private void showMenuOptionChild(MouseEvent evt) {
        System.out.println("menu option Child");
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Add new Step");
        JMenuItem item2 = new JMenuItem("Rename UT");
        JMenuItem item3 = new JMenuItem("Remove UT");
        item1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // do sth
                System.out.println("add new UT");
            }
        });
        menu.add(item1);
        menu.add(item2);
        menu.add(item3);
        menu.show(tree, evt.getPoint().x, evt.getPoint().y);
    }
    
    private void showMenuOptionChildChild(MouseEvent evt) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Remove Step");
        menu.add(item1);
        menu.show(tree, evt.getPoint().x, evt.getPoint().y);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        watchTextArea = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        fileNameLabel = new javax.swing.JLabel();
        addWatchesButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        utNameLabel = new javax.swing.JLabel();
        openButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        watchTextArea.setColumns(20);
        watchTextArea.setRows(5);
        watchTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                watchTextAreaKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(watchTextArea);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        tree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane4.setViewportView(tree);

        fileNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(TestTopComponent.class, "TestTopComponent.fileNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addWatchesButton, org.openide.util.NbBundle.getMessage(TestTopComponent.class, "TestTopComponent.addWatchesButton.text")); // NOI18N
        addWatchesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWatchesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(TestTopComponent.class, "TestTopComponent.saveButton.text")); // NOI18N

        utNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(utNameLabel, org.openide.util.NbBundle.getMessage(TestTopComponent.class, "TestTopComponent.utNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(openButton, org.openide.util.NbBundle.getMessage(TestTopComponent.class, "TestTopComponent.openButton.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fileNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jScrollPane4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(utNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 709, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addWatchesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(fileNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addComponent(utNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(openButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addWatchesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // undo redo key event
    private void watchTextAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_watchTextAreaKeyPressed
        // TODO add your handling code here:
        int ctr_shift = KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK;
        int ctr = KeyEvent.CTRL_DOWN_MASK;
        try {
            if((evt.getModifiersEx() & ctr) == ctr && evt.getKeyCode() == KeyEvent.VK_Z) {
                undoManager.undo();
            } else if((evt.getModifiersEx() & ctr) == ctr && evt.getKeyCode() == KeyEvent.VK_Y) {
                undoManager.redo();
            } else if((evt.getModifiersEx() & ctr) == ctr && evt.getKeyCode() == KeyEvent.VK_S) {
                
            }
        } catch(Exception e) {
            
        }
    }//GEN-LAST:event_watchTextAreaKeyPressed

    private void addWatchesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWatchesButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addWatchesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addWatchesButton;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton openButton;
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
