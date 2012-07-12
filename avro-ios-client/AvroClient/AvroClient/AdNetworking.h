//
//  AdRequest.h
//  FlurryAnalytics
//
//  Created by Anthony Watkins on 2/13/12.
//  Copyright (c) 2012 Flurry Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

#include "avro.h"
#include <inttypes.h>
#import <UIKit/UIKit.h>
#import "AdDelegate.h"

#define AD_REQUEST_SCHEMA \
"{\"type\":\"record\",\"name\":\"AdRequest\",\"namespace\":\"com.flurry.avroserver.protocol.v1\",\"fields\":[{\"name\":\"adSpaceName\",\"type\":\"string\"},{\"name\":\"location\",\"type\":{\"type\":\"record\",\"name\":\"Location\",\"fields\":[{\"name\":\"lat\",\"type\":\"float\",\"default\":0.0},{\"name\":\"lon\",\"type\":\"float\",\"default\":0.0}]},\"default\":\"null\"}]}"

#define LOCATION_SCHEMA \
"{\"type\":\"record\",\"name\":\"Location\",\"namespace\":\"com.flurry.avroserver.protocol.v1\",\"fields\":[{\"name\":\"lat\",\"type\":\"float\",\"default\":0.0},{\"name\":\"lon\",\"type\":\"float\",\"default\":0.0}]}"

#define AD_RESPONSE_SCHEMA \
"{\"type\":\"record\",\"name\":\"AdResponse\",\"namespace\":\"com.flurry.avroserver.protocol.v1\",\"fields\":[{\"name\":\"ads\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Ad\",\"fields\":[{\"name\":\"adSpace\",\"type\":\"string\"},{\"name\":\"adName\",\"type\":\"string\"}]}}},{\"name\":\"errors\",\"type\":{\"type\":\"array\",\"items\":\"string\"},\"default\":[]}]}"

@interface AdNetworking : NSObject {
    avro_schema_t _adRequestSchema;
    avro_schema_t _locationSchema;
    avro_schema_t _adResponseSchema;
    
    NSURLConnection *_connection;
    NSMutableData *_body;
	NSURLResponse *_response;
    id<AdDelegate> _delegate;
}

@property(nonatomic, retain) NSURLConnection *connection;
@property(nonatomic, retain) NSURLResponse *response;
@property(nonatomic, retain) NSMutableData *body;
@property(nonatomic, assign) id<AdDelegate> delegate;

- (void) initSchema;
- (void) sendAdRequest:(NSString *) adSpaceName lat:(float)lat lon:(float)lon;
- (void) sendAdRequest:(avro_datum_t) adRequest;
- (void) parseAdResponse:(NSData *)adResponse size:(NSUInteger)size;

+ (NSString *) adServerUrl;

@end
