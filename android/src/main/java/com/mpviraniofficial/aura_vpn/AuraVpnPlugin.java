package com.mpviraniofficial.aura_vpn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.anchorfree.partner.api.ClientInfo;
import com.anchorfree.partner.api.auth.AuthMethod;
import com.anchorfree.partner.api.data.Country;
import com.anchorfree.partner.api.response.AvailableCountries;
import com.anchorfree.partner.api.response.RemainingTraffic;
import com.anchorfree.partner.api.response.User;
import com.anchorfree.reporting.TrackingConstants;
import com.anchorfree.sdk.HydraTransportConfig;
import com.anchorfree.sdk.NotificationConfig;
import com.anchorfree.sdk.SdkInfo;
import com.anchorfree.sdk.SessionConfig;
import com.anchorfree.sdk.SessionInfo;
import com.anchorfree.sdk.TransportConfig;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.sdk.VpnPermissions;
import com.anchorfree.sdk.exceptions.CnlBlockedException;
import com.anchorfree.sdk.exceptions.InvalidTransportException;
import com.anchorfree.sdk.exceptions.PartnerApiException;
import com.anchorfree.sdk.fireshield.FireshieldCategory;
import com.anchorfree.sdk.fireshield.FireshieldConfig;
import com.anchorfree.sdk.rules.TrafficRule;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.callbacks.CompletableCallback;
import com.anchorfree.vpnsdk.callbacks.VpnCallback;
import com.anchorfree.vpnsdk.callbacks.VpnStateListener;
import com.anchorfree.vpnsdk.compat.CredentialsCompat;
import com.anchorfree.vpnsdk.exceptions.BrokenRemoteProcessException;
import com.anchorfree.vpnsdk.exceptions.ConnectionCancelledException;
import com.anchorfree.vpnsdk.exceptions.ConnectionTimeoutException;
import com.anchorfree.vpnsdk.exceptions.CorruptedConfigException;
import com.anchorfree.vpnsdk.exceptions.CredentialsLoadException;
import com.anchorfree.vpnsdk.exceptions.GenericPermissionException;
import com.anchorfree.vpnsdk.exceptions.InternalException;
import com.anchorfree.vpnsdk.exceptions.NetworkChangeVpnException;
import com.anchorfree.vpnsdk.exceptions.NetworkRelatedException;
import com.anchorfree.vpnsdk.exceptions.NoCredsSourceException;
import com.anchorfree.vpnsdk.exceptions.NoNetworkException;
import com.anchorfree.vpnsdk.exceptions.NoVpnTransportsException;
import com.anchorfree.vpnsdk.exceptions.ServiceBindFailedException;
import com.anchorfree.vpnsdk.exceptions.StopCancelledException;
import com.anchorfree.vpnsdk.exceptions.TrackableException;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.anchorfree.vpnsdk.exceptions.VpnPermissionDeniedException;
import com.anchorfree.vpnsdk.exceptions.VpnPermissionNotGrantedExeption;
import com.anchorfree.vpnsdk.exceptions.VpnPermissionRevokedException;
import com.anchorfree.vpnsdk.exceptions.VpnTransportException;
import com.anchorfree.vpnsdk.exceptions.WrongStateException;
import com.anchorfree.vpnsdk.transporthydra.HydraTransport;
import com.anchorfree.vpnsdk.transporthydra.HydraVpnTransportException;
import com.anchorfree.vpnsdk.vpnservice.AFVpnService;
import com.anchorfree.vpnsdk.vpnservice.ConnectionStatus;
import com.anchorfree.vpnsdk.vpnservice.VPNState;
import com.anchorfree.vpnsdk.vpnservice.credentials.AppPolicy;
import com.anchorfree.vpnsdk.vpnservice.credentials.CaptivePortalException;
import com.northghost.caketube.CaketubeTransport;
import com.northghost.caketube.OpenVpnTransportConfig;
import com.northghost.caketube.exceptions.CaketubeTransportException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import static android.content.ContentValues.TAG;

/**
 * AuraVpnPlugin
 */
public class AuraVpnPlugin implements FlutterPlugin, MethodCallHandler {

    private MethodChannel channel;
    private Context applicationContext;
    private static final String CHANNEL_ID = "vpn";
    UnifiedSDK unifiedSDK;
    private String selectedCountry = "";
    private Result result;

    private void onAttachedToEngine(Context context, BinaryMessenger messenger) {
        this.applicationContext = context;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "aura_vpn");
        channel.setMethodCallHandler(this);
        applicationContext = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        this.result = result;
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + Build.VERSION.RELEASE);
                break;
            case "initHydraSdk":
                setNewHostAndCarrier("https://dqhc6ww7pinss.cloudfront.net", "Vir_withdraws_table");
                //initHydraSdk();
                break;
            case "onStart":
                onStart();
                break;
            case "onStop":
                onStop();
                break;
            case "getVpnState":
                getVpnState();
                break;
            case "getTrafficUpdate":
                getTrafficUpdate();
                break;
            case "logOutFromVpn":
                logOutFromVpn();
                break;
            case "isConnected":
                isConnected(new Callback<Boolean>() {
                    @Override
                    public void success(@NonNull Boolean aBoolean) {
                        if (aBoolean) {
                            result.success(true);
                        }
                    }

                    @Override
                    public void failure(@NonNull VpnException e) {
                        result.success(false);
                    }
                });
                break;
            case "disconnectFromVpn":
                disconnectFromVpn();
                break;
            case "chooseServer":
                chooseServer();
                break;
            case "getCurrentServer":

                getCurrentServer(new Callback<String>() {
                    @Override
                    public void success(@NonNull String currentServer) {
                        currentServer = currentServer.equals("") ? currentServer : "OPTIMAL SERVER";
                        Map<String, String> map = new HashMap<>();
                        map.put("currentServer", currentServer);
                        result.success(map);
                    }

                    @Override
                    public void failure(@NonNull VpnException e) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("currentServer", "Optimal Server");
                        result.success(map);
                    }
                });
                break;
            case "checkRemainingTraffic":
                checkRemainingTraffic();
                break;
            case "setLoginParams":
                String hostUrl = call
                        .argument("hosturl").toString();
                String carrierId = call
                        .argument("carrierId").toString();
                setLoginParams(hostUrl, carrierId);
                break;
            case "loginUser":
                loginUser();
                break;
            case "onRegionSelected":
                Country item = call.argument("country");
                onRegionSelected(item);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    public void setNewHostAndCarrier(String hostUrl, String carrierId) {
        SharedPreferences prefs = getPrefs();
        if (TextUtils.isEmpty(hostUrl)) {
            prefs.edit().remove(BuildConfig.STORED_HOST_URL_KEY).apply();
        } else {
            prefs.edit().putString(BuildConfig.STORED_HOST_URL_KEY, hostUrl).apply();
        }

        if (TextUtils.isEmpty(carrierId)) {
            prefs.edit().remove(BuildConfig.STORED_CARRIER_ID_KEY).apply();
        } else {
            prefs.edit().putString(BuildConfig.STORED_CARRIER_ID_KEY, carrierId).apply();
        }
        initHydraSdk();
    }

    public void initHydraSdk() {
        createNotificationChannel();
        SharedPreferences prefs = getPrefs();

        final String url = prefs.getString(BuildConfig.BASE_HOST, BuildConfig.BASE_HOST);
        final String carrier = prefs.getString(BuildConfig.STORED_CARRIER_ID_KEY, BuildConfig.BASE_CARRIER_ID);
        ClientInfo clientInfo = ClientInfo.newBuilder()
                .addUrl(url)
                .carrierId(carrier)
                .build();

        List<TransportConfig> transportConfigList = new ArrayList<>();
        transportConfigList.add(HydraTransportConfig.create());
        transportConfigList.add(OpenVpnTransportConfig.tcp());
        transportConfigList.add(OpenVpnTransportConfig.udp());

        UnifiedSDK.update(transportConfigList, CompletableCallback.EMPTY);

        NotificationConfig notificationConfig = NotificationConfig.newBuilder()
                .title(applicationContext.getResources().getString(R.string.app_name))
                .build();

        UnifiedSDK.update(notificationConfig);

        UnifiedSDK.clearInstances();
        unifiedSDK = UnifiedSDK.getInstance(clientInfo);
        UnifiedSDK.setLoggingLevel(Log.VERBOSE);

        result.success(true);
    }

    public SharedPreferences getPrefs() {
        return applicationContext.getSharedPreferences(BuildConfig.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sample VPN";
            String description = "VPN notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = applicationContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

   /* private void initHydraSdk() {
        final ClientInfo clientInfo = ClientInfo.newBuilder()
                .addUrls(Collections.singletonList("https://dqhc6ww7pinss.cloudfront.net"))
                .carrierId("Vir_withdraws_table")
                .build();

        NotificationConfig notificationConfig = NotificationConfig.newBuilder()
                .title(applicationContext.getResources().getString(R.string.app_name))
                .build();

        HydraSdk.setLoggingLevel(Log.VERBOSE);

        HydraSDKConfig config = HydraSDKConfig.newBuilder()
                .observeNetworkChanges(true) //sdk will handle network changes and start/stop vpn
                .captivePortal(true) //sdk will handle if user is behind captive portal wifi
                .moveToIdleOnPause(false)//sdk will report PAUSED state
                .build();
        HydraSdk.init(applicationContext, clientInfo, notificationConfig, config);


    }*/

    protected void onStart() {

        PackageManager pm = applicationContext.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(applicationContext, AFVpnService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    protected void getVpnState() {
        UnifiedSDK.addVpnStateListener(new VpnStateListener() {
            @Override
            public void vpnStateChanged(@NonNull VPNState vpnState) {

                switch (vpnState) {
                    case UNKNOWN:
                        result.success(VPNState.UNKNOWN.toString());
                        break;
                    case CONNECTED:
                        result.success(VPNState.CONNECTED.toString());
                        break;
                    case IDLE:
                        result.success(VPNState.IDLE.toString());
                        break;
                    case PAUSED:
                        result.success(VPNState.PAUSED.toString());
                        break;
                    case CONNECTING_CREDENTIALS:
                        result.success(VPNState.CONNECTING_CREDENTIALS.toString());
                        break;
                    case CONNECTING_PERMISSIONS:
                        result.success(VPNState.CONNECTING_PERMISSIONS.toString());
                        break;
                    case CONNECTING_VPN:
                        result.success(VPNState.CONNECTING_VPN.toString());
                        break;
                    case DISCONNECTING:
                        result.success(VPNState.DISCONNECTING.toString());
                        break;
                    case ERROR:
                        result.success(VPNState.ERROR.toString());
                        break;
                    default:
                        result.success(VPNState.UNKNOWN.toString());
                        break;
                }
            }

            @Override
            public void vpnError(@NonNull VpnException e) {

                result.success(VPNState.UNKNOWN.toString());

            }
        });
    }

    protected void getTrafficUpdate() {
        UnifiedSDK.addTrafficListener((tx, rx) -> {

            //handle used traffic update
            //tx - bytes transfered
            //rx - bytes received

        });

    }

    protected void onStop() {
//        UnifiedSDK.removeVpnStateListener(this);
//        UnifiedSDK.removeTrafficListener(this);
//        UnifiedSDK.removeTrafficListener(this);
//        UnifiedSDK.removeVpnStateListener(this);
    }


//    @Override
//    public void onTrafficUpdate(long bytesTx, long bytesRx) {
////                 updateUI();
////                 updateTrafficStats(bytesTx, bytesRx);
//    }

//    @Override
//    public void vpnStateChanged(VPNState vpnState) {
//                 updateUI();
//    }
//
//    @Override
//    public void vpnError(VpnException e) {
//                 updateUI();
//        handleError(e);
//    }

    protected void isLoggedIn(Callback<Boolean> callback) {
        unifiedSDK.getBackend().isLoggedIn(callback);
    }

    protected void loginToVpn() {
//                 showLoginProgress();
        AuthMethod authMethod = AuthMethod.anonymous();
        unifiedSDK.getBackend().login(authMethod, new Callback<User>() {
            @Override
            public void success(@NonNull User user) {
//                         hideLoginProgress();
//                         updateUI();
                result.success(true);
                showMessage(user.getAccessToken());

            }

            @Override
            public void failure(@NonNull VpnException e) {
//                         hideLoginProgress();
//                         updateUI();
                showMessage(e.getMessage());
                result.success(false);
                result.error(e.getGprReason(), e.getMessage(), e.getLocalizedMessage());
                handleError(e);
            }
        });
    }


    protected void logOutFromVpn() {
//                 showLoginProgress();

        unifiedSDK.getBackend().logout(new CompletableCallback() {
            @Override
            public void complete() {
//                         hideLoginProgress();
//                         updateUI();
            }

            @Override
            public void error(VpnException e) {
//                         hideLoginProgress();
//                         updateUI();
            }
        });
        selectedCountry = "";
    }


    protected void isConnected(Callback<Boolean> callback) {
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {
                callback.success(vpnState == VPNState.CONNECTED);
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.success(false);
            }
        });
    }

    protected void connectToVpn() {
        //set selectedCountry  = "" for optimal country
        selectedCountry = selectedCountry.equals("") ? selectedCountry : UnifiedSDK.COUNTRY_OPTIMAL;

        isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    List<String> fallbackOrder = new ArrayList<>();
                    fallbackOrder.add(HydraTransport.TRANSPORT_ID);
                    fallbackOrder.add(CaketubeTransport.TRANSPORT_ID_TCP);
                    fallbackOrder.add(CaketubeTransport.TRANSPORT_ID_UDP);
//                             showConnectProgress();
                    List<String> bypassDomains = new LinkedList<>();
                    bypassDomains.add("*domain1.com");
                    bypassDomains.add("*domain2.com");
                    unifiedSDK.getVPN().start(new SessionConfig.Builder()
                            .withReason(TrackingConstants.GprReasons.M_UI)
                            .withTransportFallback(fallbackOrder)
                            .withTransport(HydraTransport.TRANSPORT_ID)
                            .withVirtualLocation(selectedCountry)
                            .addDnsRule(TrafficRule.Builder.bypass().fromDomains(bypassDomains))
                            .build(), new CompletableCallback() {
                        @Override
                        public void complete() {
//                                     hideConnectProgress();
//                                     startUIUpdateTask();
                        }

                        @Override
                        public void error(@NonNull VpnException e) {
//                                     hideConnectProgress();
//                                     updateUI();

                            handleError(e);
                        }
                    });
                } else {
                    showMessage("Login please");
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    protected void disconnectFromVpn() {
//                 showConnectProgress();
        unifiedSDK.getVPN().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
            @Override
            public void complete() {
//                         hideConnectProgress();
//                         stopUIUpdateTask(true);
            }

            @Override
            public void error(VpnException e) {
//                         hideConnectProgress();
//                         updateUI();

                handleError(e);
            }
        });
    }

    protected void chooseServer() {
        isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    loadServers();
//                       RegionChooserDialog.newInstance().show(getSupportFragmentManager(), RegionChooserDialog.TAG);
                } else {
                    showMessage("Login please");
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    private void loadServers() {
        unifiedSDK.getBackend().countries(new Callback<AvailableCountries>() {
            @Override
            public void success(@NonNull AvailableCountries countries) {
                showMessage(countries.getCountries().toString());
                result.success(countries.getCountries().toString());
            }

            @Override
            public void failure(@NonNull VpnException e) {
                result.success(false);
            }
        });
    }


    protected void getCurrentServer(final Callback<String> callback) {
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState state) {
                if (state == VPNState.CONNECTED) {
                    UnifiedSDK.getStatus(new Callback<SessionInfo>() {
                        @Override
                        public void success(@NonNull SessionInfo sessionInfo) {
                            callback.success(CredentialsCompat.getServerCountry(sessionInfo.getCredentials()));
                        }

                        @Override
                        public void failure(@NonNull VpnException e) {
                            callback.success(selectedCountry);
                        }
                    });
                } else {
                    callback.success(selectedCountry);
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.failure(e);
            }
        });
    }


    protected void checkRemainingTraffic() {
        unifiedSDK.getBackend().remainingTraffic(new Callback<RemainingTraffic>() {
            @Override
            public void success(@NonNull RemainingTraffic remainingTraffic) {
//                         updateRemainingTraffic(remainingTraffic);
            }

            @Override
            public void failure(@NonNull VpnException e) {
//                         updateUI();
                handleError(e);
            }
        });
    }


    public void setLoginParams(String hostUrl, String carrierId) {
        setNewHostAndCarrier(hostUrl, carrierId);
    }

    public void loginUser() {
        loginToVpn();
    }

    public void onRegionSelected(Country item) {
//        selectedCountry = "";
        selectedCountry = item == null ? "" : item.getCountry();
//                 updateUI();
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState state) {
                if (state == VPNState.CONNECTED) {
                    showMessage("Reconnecting to VPN with " + selectedCountry);
                    unifiedSDK.getVPN().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
                        @Override
                        public void complete() {
                            connectToVpn();
                        }

                        @Override
                        public void error(VpnException e) {
                            // In this case we try to reconnect
                            selectedCountry = "";
                            connectToVpn();
                        }
                    });
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    // Example of error handling
    public void handleError(Throwable e) {
        Log.w(TAG, e);
        if (e instanceof NetworkRelatedException) {
            showMessage("Check internet connection");
        } else if (e instanceof VpnException) {
            if (e instanceof VpnPermissionRevokedException) {
                showMessage("User revoked vpn permissions");
            } else if (e instanceof VpnPermissionDeniedException) {
                showMessage("User canceled to grant vpn permissions");
            } else if (e instanceof HydraVpnTransportException) {
                HydraVpnTransportException hydraVpnTransportException = (HydraVpnTransportException) e;
                if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_ERROR_BROKEN) {
                    showMessage("Connection with vpn server was lost");
                } else if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_DCN_BLOCKED_BW) {
                    showMessage("Client traffic exceeded");
                } else {
                    showMessage("Error in VPN transport");
                }
            } else if (e instanceof PartnerApiException) {
                switch (((PartnerApiException) e).getContent()) {
                    case PartnerApiException.CODE_NOT_AUTHORIZED:
                        showMessage("User unauthorized");
                        break;
                    case PartnerApiException.CODE_TRAFFIC_EXCEED:
                        showMessage("Server unavailable");
                        break;
                    default:
                        showMessage("Other error. Check PartnerApiException constants");
                        break;
                }
            }
        } else {
            showMessage("Error in VPN Service");
        }
    }

    protected void showMessage(String msg) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show();
    }

    //fake method to support migration documentation and list all available methods
    public void sdkMethodsList() {
        final UnifiedSDK instance = unifiedSDK;
        ClientInfo.newBuilder().addUrl("").carrierId("test").addUrls(new ArrayList<>()).build();
        instance.getBackend().deletePurchase(0, CompletableCallback.EMPTY);
        instance.getVPN().getStartTimestamp(new Callback<Long>() {
            @Override
            public void success(@NonNull Long aLong) {

            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {

            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        UnifiedSDK.getConnectionStatus(new Callback<ConnectionStatus>() {
            @Override
            public void success(@NonNull ConnectionStatus connectionStatus) {

            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        instance.getBackend().remainingTraffic(new Callback<RemainingTraffic>() {
            @Override
            public void success(@NonNull RemainingTraffic remainingTraffic) {

            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        instance.getVPN().restart(new SessionConfig.Builder()
                .withReason(TrackingConstants.GprReasons.M_UI)
                .addDnsRule(TrafficRule.Builder.blockDns().fromAssets(""))
                .addProxyRule(TrafficRule.Builder.blockPkt().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.bypass().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.proxy().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.vpn().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.vpn().fromDomains(new ArrayList<>()))
                .addDnsRule(TrafficRule.Builder.vpn().fromFile(""))
                .addDnsRule(TrafficRule.Builder.vpn().fromResource(0))
                .exceptApps(new ArrayList<>())
                .forApps(new ArrayList<>())
                .withVirtualLocation("")
                .withPolicy(AppPolicy.newBuilder().build())
                .withFireshieldConfig(new FireshieldConfig.Builder()
                        .addCategory(FireshieldCategory.Builder.block(""))
                        .addCategory(FireshieldCategory.Builder.blockAlertPage(""))
                        .addCategory(FireshieldCategory.Builder.bypass(""))
                        .addCategory(FireshieldCategory.Builder.custom("", ""))
                        .addCategory(FireshieldCategory.Builder.proxy(""))
                        .addCategory(FireshieldCategory.Builder.vpn(""))
                        .build())
                .build(), new CompletableCallback() {
            @Override
            public void complete() {

            }

            @Override
            public void error(@NonNull VpnException e) {

            }
        });

        UnifiedSDK.setLoggingLevel(Log.VERBOSE);

        instance.getBackend().purchase("", new CompletableCallback() {
            @Override
            public void complete() {

            }

            @Override
            public void error(VpnException e) {

            }
        });
        instance.getBackend().purchase("", "", new CompletableCallback() {
            @Override
            public void complete() {

            }

            @Override
            public void error(VpnException e) {

            }
        });
        unifiedSDK.getInfo(new Callback<SdkInfo>() {
            @Override
            public void success(@NonNull SdkInfo sdkInfo) {
                String deviceId = sdkInfo.getDeviceId();
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });

        instance.getBackend().currentUser(new Callback<User>() {
            @Override
            public void success(@NonNull User user) {

            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        UnifiedSDK.getStatus(new Callback<SessionInfo>() {
            @Override
            public void success(@NonNull SessionInfo sessionInfo) {

            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        unifiedSDK.getBackend().getAccessToken();

        VpnPermissions.request(new CompletableCallback() {
            @Override
            public void complete() {

            }

            @Override
            public void error(VpnException e) {

            }
        });

        UnifiedSDK.addVpnCallListener(new VpnCallback() {
            @Override
            public void onVpnCall(Parcelable parcelable) {

            }
        });
        UnifiedSDK.removeVpnCallListener(null);

        instance.getVPN().updateConfig(new SessionConfig.Builder()
                .withReason(TrackingConstants.GprReasons.M_UI)
                .addDnsRule(TrafficRule.Builder.blockDns().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.blockPkt().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.bypass().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.proxy().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.vpn().fromAssets(""))
                .addDnsRule(TrafficRule.Builder.vpn().fromDomains(new ArrayList<>()))
                .addDnsRule(TrafficRule.Builder.vpn().fromFile(""))
                .addDnsRule(TrafficRule.Builder.vpn().fromResource(0))
                .addDnsRule(TrafficRule.Builder.vpn().fromIp("", 0))
                .addDnsRule(TrafficRule.Builder.vpn().fromIp("", 0, 0))
                .addDnsRule(TrafficRule.Builder.vpn().fromIp("", 0, 0, 0))
                .addDnsRule(TrafficRule.Builder.vpn().tcp())
                .addDnsRule(TrafficRule.Builder.vpn().tcp(0))
                .addDnsRule(TrafficRule.Builder.vpn().tcp(0, 0))
                .addDnsRule(TrafficRule.Builder.vpn().udp())
                .addDnsRule(TrafficRule.Builder.vpn().udp(0))
                .addDnsRule(TrafficRule.Builder.vpn().udp(0, 0))
                .exceptApps(new ArrayList<>())
                .withTransport("")
                .withSessionId("")
                .forApps(new ArrayList<>())
                .withVirtualLocation("")
                .withPolicy(AppPolicy.newBuilder().build())
                .withFireshieldConfig(new FireshieldConfig.Builder()
                        .addCategory(FireshieldCategory.Builder.block(""))
                        .addCategory(FireshieldCategory.Builder.blockAlertPage(""))
                        .addCategory(FireshieldCategory.Builder.bypass(""))
                        .addCategory(FireshieldCategory.Builder.custom("", ""))
                        .addCategory(FireshieldCategory.Builder.proxy(""))
                        .addCategory(FireshieldCategory.Builder.vpn(""))
                        .build())
                .build(), new CompletableCallback() {
            @Override
            public void complete() {

            }

            @Override
            public void error(VpnException e) {

            }
        });
        //exceptions
        Class[] ex = new Class[]{
                VpnException.class,
                BrokenRemoteProcessException.class,
                CaketubeTransportException.class,
                CaptivePortalException.class,
                CnlBlockedException.class,
                ConnectionCancelledException.class,
                ConnectionTimeoutException.class,
                CorruptedConfigException.class,
                CredentialsLoadException.class,
                GenericPermissionException.class,
                HydraVpnTransportException.class,
                InternalException.class,
                InvalidTransportException.class,
                NetworkChangeVpnException.class,
                NetworkRelatedException.class,
                NoCredsSourceException.class,
                NoNetworkException.class,
                NoVpnTransportsException.class,
                PartnerApiException.class,
                ServiceBindFailedException.class,
                StopCancelledException.class,
                TrackableException.class,
                VpnPermissionDeniedException.class,
                VpnPermissionRevokedException.class,
                VpnPermissionNotGrantedExeption.class,
                VpnTransportException.class,
                WrongStateException.class
        };
        String[] serrors = new String[]{
                PartnerApiException.CODE_PARSE_EXCEPTION,
                PartnerApiException.CODE_SESSIONS_EXCEED,
                PartnerApiException.CODE_DEVICES_EXCEED,
                PartnerApiException.CODE_INVALID,
                PartnerApiException.CODE_OAUTH_ERROR,
                PartnerApiException.CODE_TRAFFIC_EXCEED,
                PartnerApiException.CODE_NOT_AUTHORIZED,
                PartnerApiException.CODE_SERVER_UNAVAILABLE,
                PartnerApiException.CODE_INTERNAL_SERVER_ERROR,
                PartnerApiException.CODE_USER_SUSPENDED,
        };
        Integer[] errors = new Integer[]{
                CaketubeTransportException.CONNECTION_BROKEN_ERROR,
                CaketubeTransportException.CONNECTION_FAILED_ERROR,
                CaketubeTransportException.CONNECTION_AUTH_FAILURE,
                HydraVpnTransportException.HYDRA_CONNECTION_LOST,
                HydraVpnTransportException.TRAFFIC_EXCEED,
                HydraVpnTransportException.HYDRA_CANNOT_CONNECT,
                HydraVpnTransportException.HYDRA_ERROR_CONFIG,
                HydraVpnTransportException.HYDRA_ERROR_UNKNOWN,
                HydraVpnTransportException.HYDRA_ERROR_CONFIGURATION,
                HydraVpnTransportException.HYDRA_ERROR_BROKEN,
                HydraVpnTransportException.HYDRA_ERROR_INTERNAL,
                HydraVpnTransportException.HYDRA_ERROR_SERVER_AUTH,
                HydraVpnTransportException.HYDRA_ERROR_CANT_SEND,
                HydraVpnTransportException.HYDRA_ERROR_TIME_SKEW,
                HydraVpnTransportException.HYDRA_DCN_MIN,
                HydraVpnTransportException.HYDRA_DCN_SRV_SWITCH,
                HydraVpnTransportException.HYDRA_DCN_BLOCKED_BW,
                HydraVpnTransportException.HYDRA_DCN_BLOCKED_ABUSE,
                HydraVpnTransportException.HYDRA_DCN_BLOCKED_MALWARE,
                HydraVpnTransportException.HYDRA_DCN_BLOCKED_MISC,
                HydraVpnTransportException.HYDRA_DCN_REQ_BY_CLIAPP,
                HydraVpnTransportException.HYDRA_DCN_BLOCKED_AUTH,
                HydraVpnTransportException.HYDRA_DCN_MAX,
                HydraVpnTransportException.HYDRA_NOTIFY_AUTH_OK,
                HydraVpnTransportException.HYDRA_CONFIG_MALFORMED,
                VpnTransportException.TRANSPORT_ERROR_START_TIMEOUT,
        };
    }
}
