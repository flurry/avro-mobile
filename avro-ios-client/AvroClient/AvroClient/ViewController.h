//
//  ViewController.h
//  AvroClient
//
//  Created by Anthony Watkins on 7/9/12.
//  Copyright (c) 2012 Flurry, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AdDelegate.h"

@class AdNetworking;

@interface ViewController : UIViewController <AdDelegate> {
    UITextField*    _adSpaceName;
    UIButton*       _sendRequest;
    UITextView*    _responseText;
    
    AdNetworking*   _network;
}

@property (nonatomic, retain) IBOutlet UITextField  *adSpaceName;
@property (nonatomic, retain) IBOutlet UIButton     *sendRequest;
@property (nonatomic, retain) IBOutlet UITextView *responseText;
@property (nonatomic, retain) AdNetworking *network;

-(IBAction) sendRequestClickedButton:(id)sender;

@end
