#import "AuraVpnPlugin.h"
#if __has_include(<aura_vpn/aura_vpn-Swift.h>)
#import <aura_vpn/aura_vpn-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "aura_vpn-Swift.h"
#endif

@implementation AuraVpnPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAuraVpnPlugin registerWithRegistrar:registrar];
}
@end
