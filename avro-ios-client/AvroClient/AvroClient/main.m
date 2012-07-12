//
//  main.m
//  AvroClient
//
//  Created by Anthony Watkins on 7/9/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AppDelegate.h"

int main(int argc, char *argv[])
{
    @autoreleasepool {
        @try {
        return UIApplicationMain(argc, argv, nil, NSStringFromClass([AppDelegate class]));
        }  @catch (NSException *exception) {
        NSLog(@"Exception - %@",[exception description]);
        exit(EXIT_FAILURE);
        }
    }
}
