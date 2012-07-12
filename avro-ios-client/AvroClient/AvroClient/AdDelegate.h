//
//  AdDelegate.h
//  AdDelegate
//
//  Created by Anthony Watkins.
//
#import <UIKit/UIKit.h>

@protocol AdDelegate <NSObject>

@optional
/*
 Called when we receive an ad response
 */
- (void)adResponseReceived:(NSString *)response;
- (void)adRequestDidFinsih;

@end
