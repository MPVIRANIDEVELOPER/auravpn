import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:aura_vpn/aura_vpn.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  String init = "Init";
  String login = "Log in";
  String connect = "Connect";
  String current_server = "Current Server";

  @override
  void initState() {
    super.initState();
    AuraVpn.initHydraSdk;
    AuraVpn.onStart;
  }

  @override
  void dispose() {
    AuraVpn.onStop;
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Aura VPN'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              ElevatedButton(
                child: Text(init),
                onPressed: () async {
                  bool result = await AuraVpn.initHydraSdk;
                  print(result);
                },
              ),
              ElevatedButton(
                child: Text(login),
                onPressed: () async {
                  bool result = await AuraVpn.loginUser;
                  print(result);
                },
              ),
              ElevatedButton(
                child: Text(connect),
                onPressed: () async {
                  bool result = await AuraVpn.onRegionSelected;
                  print(result);
                },
              ),
              ElevatedButton(
                child: Text(current_server),
                onPressed: () async {
                  var server_list = await AuraVpn.chooseServer;
                  print(server_list.toString());
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
