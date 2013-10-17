package com.github.veithen.visualwas;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import com.github.veithen.visualwas.env.EnvUtil;
import com.github.veithen.visualwas.jmx.soap.SOAPJMXConnector;
import com.github.veithen.visualwas.trust.NotTrustedException;
import com.sun.tools.visualvm.core.properties.PropertiesPanel;
import com.sun.tools.visualvm.core.ui.components.Spacer;

public class WebSpherePropertiesPanel extends PropertiesPanel {
    private static final long serialVersionUID = -5821630337324177997L;
    
    private static ImageIcon connectingIcon = new ImageIcon(WebSpherePropertiesPanel.class.getResource("connecting.gif"));
    private static ImageIcon errorIcon = new ImageIcon(WebSpherePropertiesPanel.class.getResource("error.png"));
    private static ImageIcon okIcon = new ImageIcon(WebSpherePropertiesPanel.class.getResource("ok.png"));
    
    private final JTextField hostField;
    private final JTextField portField;
    private final JCheckBox securityCheckbox;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JCheckBox saveCheckbox;
    private final JButton testConnectionButton;
    private final JLabel testConnectionResult;
    
    public WebSpherePropertiesPanel() {
        setLayout(new GridBagLayout());

        {
            JLabel hostLabel = new JLabel();
            Mnemonics.setLocalizedText(hostLabel, NbBundle.getMessage(WebSpherePropertiesPanel.class, "LBL_Host"));
            add(hostLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(2, 0, 2, 0), 0, 0));

            hostField = new JTextField();
            hostLabel.setLabelFor(hostField);
            hostField.getDocument().addDocumentListener(new SimpleDocumentListener() {
                @Override
                protected void updated() {
                    update();
                    resetConnectionTestResults();
                }
            });
            add(hostField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, new Insets(2, 5, 2, 0), 0, 0));
        }

        {
            JLabel portLabel = new JLabel();
            Mnemonics.setLocalizedText(portLabel, NbBundle.getMessage(WebSpherePropertiesPanel.class, "LBL_Port"));
            add(portLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(2, 0, 2, 0), 0, 0));

            portField = new JTextField(6);
            portLabel.setLabelFor(portField);
            portField.getDocument().addDocumentListener(new SimpleDocumentListener() {
                @Override
                protected void updated() {
                    update();
                    resetConnectionTestResults();
                }
            });
            add(portField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, WEST, NONE, new Insets(2, 5, 2, 0), 0, 0));
        }

        {
            securityCheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(securityCheckbox, NbBundle.getMessage(WebSpherePropertiesPanel.class, "LBL_Security_enabled"));
            securityCheckbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update();
                    resetConnectionTestResults();
                };
            });
            add(securityCheckbox, new GridBagConstraints(0, 2, REMAINDER, 1, 0.0, 0.0, WEST, NONE, new Insets(8, 0, 2, 0), 0, 0));
        }

        {
            JLabel usernameLabel = new JLabel();
            Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(WebSpherePropertiesPanel.class, "LBL_Username"));
            add(usernameLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(2, 15, 2, 0), 0, 0));
    
            usernameField = new JTextField(12);
            usernameLabel.setLabelFor(usernameField);
            usernameField.getDocument().addDocumentListener(new SimpleDocumentListener() {
                @Override
                protected void updated() {
                    update();
                    resetConnectionTestResults();
                }
            });
            add(usernameField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, WEST, NONE, new Insets(2, 5, 2, 0), 0, 0));
        }

        {
            JLabel passwordLabel = new JLabel();
            Mnemonics.setLocalizedText(passwordLabel, NbBundle.getMessage(WebSpherePropertiesPanel.class, "LBL_Password"));
            add(passwordLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(2, 15, 2, 0), 0, 0));
    
            passwordField = new JPasswordField(12);
            passwordLabel.setLabelFor(passwordField);
            passwordField.getDocument().addDocumentListener(new SimpleDocumentListener() {
                @Override
                protected void updated() {
                    update();
                    resetConnectionTestResults();
                }
            });
            add(passwordField, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, WEST, NONE, new Insets(2, 5, 2, 0), 0, 0));
        }

        {
            saveCheckbox = new JCheckBox();   // NOI18N
            Mnemonics.setLocalizedText(saveCheckbox, NbBundle.getMessage(WebSpherePropertiesPanel.class, "LBL_Save_security_credentials"));
            saveCheckbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update();
                };
            });
            add(saveCheckbox, new GridBagConstraints(0, 5, REMAINDER, 1, 0.0, 0.0, WEST, NONE, new Insets(2, 15, 2, 0), 0, 0));
        }
        
        {
            testConnectionButton = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    testConnection();
                }
            });
            Mnemonics.setLocalizedText(testConnectionButton, NbBundle.getMessage(WebSpherePropertiesPanel.class, "LBL_Test_connection"));
            add(testConnectionButton, new GridBagConstraints(0, 6, REMAINDER, 1, 0.0, 0.0, WEST, NONE, new Insets(5, 0, 2, 0), 0, 0));
        }
        
        {
            testConnectionResult = new JLabel();
            testConnectionResult.setBorder(BorderFactory.createEtchedBorder());
            testConnectionResult.setBackground(Color.WHITE);
            testConnectionResult.setOpaque(true);
            testConnectionResult.setPreferredSize(new Dimension(50, 50));
            add(testConnectionResult, new GridBagConstraints(0, 7, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, new Insets(2, 0, 2, 0), 0, 0));
        }
        
        add(Spacer.create(), new GridBagConstraints(0, 8, 2, 1, 1.0, 1.0, NORTHWEST, BOTH, new Insets(0, 0, 0, 0), 0, 0));
        
        update();
    }

    public String getHost() {
        return hostField.getText();
    }
    
    public void setHost(String host) {
        hostField.setText(host);
    }
    
    public int getPort() {
        try {
            return Integer.parseInt(portField.getText());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
    
    public void setPort(int port) {
        portField.setText(String.valueOf(port));
    }
    
    public boolean isSecurityEnabled() {
        return securityCheckbox.isSelected();
    }
    
    public void setSecurityEnabled(boolean securityEnabled) {
        securityCheckbox.setSelected(securityEnabled);
    }
    
    public String getUsername() {
        return isSecurityEnabled() ? usernameField.getText() : null;
    }

    public void setUsername(String username) {
        usernameField.setText(username);
    }
    
    public char[] getPassword() {
        return isSecurityEnabled() ? passwordField.getPassword() : null;
    }
    
    public void setPassword(String password) {
        passwordField.setText(password);
    }
    
    public boolean isSaveCredentials() {
        return isSecurityEnabled() && saveCheckbox.isSelected();
    }
    
    public void setSaveCredentials(boolean saveCredentials) {
        saveCheckbox.setSelected(saveCredentials);
    }
    
    private void update() {
        int port = getPort();
        testConnectionButton.setEnabled(getHost().length() > 0 && port > 0 && port < 1<<16);
        
        boolean securityEnabled = securityCheckbox.isSelected();
        usernameField.setEnabled(securityEnabled);
        passwordField.setEnabled(securityEnabled);
        saveCheckbox.setEnabled(securityEnabled);
    }

    private void testConnection() {
        testConnectionButton.setEnabled(false);
        testConnectionResult.setIcon(connectingIcon);
        testConnectionResult.setText(NbBundle.getMessage(WebSpherePropertiesPanel.class, "MSG_Connecting"));
        final JMXServiceURL serviceURL;
        try {
            serviceURL = new JMXServiceURL("soap", getHost(), getPort());
        } catch (MalformedURLException ex) {
            // We should never get here
            throw new Error(ex);
        }
        boolean securityEnabled = isSecurityEnabled();
        final Map<String,Object> env = EnvUtil.createEnvironment(securityEnabled);
        if (securityEnabled) {
            env.put(JMXConnector.CREDENTIALS, new String[] { getUsername(), new String(getPassword()) });
        }
        env.put(SOAPJMXConnector.CONNECT_TIMEOUT, 8000);
        new Thread() {
            @Override
            public void run() {
                IOException tmpException = null;
                try {
                    JMXConnectorFactory.connect(serviceURL, env);
                } catch (IOException ex) {
                    tmpException = ex;
                }
                final IOException exception = tmpException;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        testConnectionButton.setEnabled(true);
                        if (exception == null) {
                            testConnectionResult.setIcon(okIcon);
                            testConnectionResult.setText(NbBundle.getMessage(WebSpherePropertiesPanel.class, "MSG_Connection_successful"));
                            setSettingsValid(true);
                        } else if (exception instanceof SSLHandshakeException && exception.getCause() instanceof NotTrustedException) {
                            X509Certificate[] chain = ((NotTrustedException)exception.getCause()).getChain();
                            SignerExchangeDialog dialog = new SignerExchangeDialog(SwingUtilities.getWindowAncestor(WebSpherePropertiesPanel.this), chain);
                            if (!dialog.showDialog()) {
                                resetConnectionTestResults();
                            } else {
                                testConnection();
                            }
                        } else {
                            exception.printStackTrace();
                            testConnectionResult.setIcon(errorIcon);
                            testConnectionResult.setText(exception.getClass().getSimpleName() + ": " + exception.getMessage());
                            setSettingsValid(false);
                        }
                    }
                });
            }
        }.start();
    }
    
    private void resetConnectionTestResults() {
        testConnectionResult.setIcon(null);
        testConnectionResult.setText(null);
        setSettingsValid(false);
    }
}
