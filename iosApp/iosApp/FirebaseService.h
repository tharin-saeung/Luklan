#import <Foundation/Foundation.h>

@interface FirebaseService : NSObject

+ (instancetype)shared;

- (void)addDocumentWithCollection:(NSString *)collection
                       documentId:(NSString *)documentId
                             data:(NSDictionary *)data
                       completion:(void (^)(NSError *))completion;

- (void)getDocumentsWithCollection:(NSString *)collection
                             field:(NSString *)field
                             value:(NSString *)value
                        completion:(void (^)(NSArray<NSDictionary *> *, NSError *))completion;

- (void)updateDocumentWithCollection:(NSString *)collection
                          documentId:(NSString *)documentId
                                data:(NSDictionary *)data
                          completion:(void (^)(NSError *))completion;

@end
