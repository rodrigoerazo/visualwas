package com.github.veithen.visualwas.trust;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.prefs.Preferences;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

import org.openide.util.NbPreferences;

public final class TrustStore {
    private static final String PROP_KEY = "trustStore";
    
    private static TrustStore instance;
    
    private final Preferences prefs;
    
    private TrustStore() {
        prefs = NbPreferences.forModule(TrustStore.class);
    }
    
    public static TrustStore getInstance() {
        if (instance == null) {
            instance = new TrustStore();
        }
        return instance;
    }
    
    private KeyStore getTrustStore() throws GeneralSecurityException {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        byte[] trustStoreContent = prefs.getByteArray(PROP_KEY, null);
        try {
            trustStore.load(trustStoreContent == null ? null : new ByteArrayInputStream(trustStoreContent), new char[0]);
        } catch (IOException ex) {
            // We should never get here
        }
        return trustStore;
    }
    
    /**
     * Create a {@link TrustManager} that validates server certificates against this trust store.
     * The returned trust manager is configured to throw a {@link NotTrustedException} with the
     * certificates presented by the server if they are not trusted.
     * 
     * @return the trust manager
     * @throws GeneralSecurityException
     */
    public TrustManager createTrustManager() throws GeneralSecurityException {
        KeyStore trustStore = getTrustStore();
        if (trustStore.aliases().hasMoreElements()) {
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(trustStore);
            TrustManager[] trustManagers = tmfactory.getTrustManagers();
            if (trustManagers.length != 1) {
                throw new RuntimeException("Expected a TrustManager array with a single entry");
            }
            return new TrustManagerWrapper((X509ExtendedTrustManager)trustManagers[0]);
        } else {
            return new NoTrustManager();
        }
    }

    public void addCertificate(X509Certificate cert) throws GeneralSecurityException {
        KeyStore trustStore = getTrustStore();
        trustStore.setCertificateEntry(String.valueOf(System.currentTimeMillis()), cert);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            trustStore.store(baos, new char[0]);
        } catch (IOException ex) {
            // We should never get here
        }
        prefs.putByteArray(PROP_KEY, baos.toByteArray());
    }
    
    public void export(File file, char[] password) throws GeneralSecurityException, IOException {
        KeyStore trustStore = getTrustStore();
        FileOutputStream out = new FileOutputStream(file);
        try {
            trustStore.store(out, password);
        } finally {
            out.close();
        }
    }
}
