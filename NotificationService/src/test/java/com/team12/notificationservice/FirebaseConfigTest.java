package com.team12.notificationservice;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.team12.notificationservice.config.FirebaseConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class FirebaseConfigTest {

    @Test
    void init_returnsEarly_whenAlreadyInitialized() throws IOException {
        FirebaseConfig cfg = new FirebaseConfig();

        try (MockedStatic<FirebaseApp> appStatic = mockStatic(FirebaseApp.class)) {
            appStatic.when(FirebaseApp::getApps)
                    .thenReturn(List.of(mock(FirebaseApp.class)));

            cfg.init();

            appStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), never());
        }
    }

    @Test
    void init_initializes_whenNoApps() throws IOException {
        FirebaseConfig cfg = new FirebaseConfig();

        // mocks
        GoogleCredentials creds = mock(GoogleCredentials.class);
        FirebaseOptions.Builder builder = mock(FirebaseOptions.Builder.class);
        FirebaseOptions options = mock(FirebaseOptions.class);

        when(builder.setCredentials(any())).thenReturn(builder);
        when(builder.build()).thenReturn(options);

        try (MockedStatic<FirebaseApp> appStatic = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> credStatic = mockStatic(GoogleCredentials.class);
             MockedStatic<FirebaseOptions> optStatic = mockStatic(FirebaseOptions.class)) {

            appStatic.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());
            credStatic.when(GoogleCredentials::getApplicationDefault).thenReturn(creds);
            optStatic.when(FirebaseOptions::builder).thenReturn(builder);

            cfg.init();

            optStatic.verify(FirebaseOptions::builder, times(1));
            verify(builder).setCredentials(creds);
            verify(builder).build();
            appStatic.verify(() -> FirebaseApp.initializeApp(options), times(1));
        }
    }
}
