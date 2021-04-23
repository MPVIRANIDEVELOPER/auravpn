import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:aura_vpn/aura_vpn.dart';

void main() {
  const MethodChannel channel = MethodChannel('aura_vpn');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AuraVpn.platformVersion, '42');
  });
}
