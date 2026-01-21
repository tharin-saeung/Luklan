#import "FirebaseService.h"
@import FirebaseFirestore;

@implementation FirebaseService

+ (instancetype)shared {
    static FirebaseService *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (void)addDocumentWithCollection:(NSString *)collection
                       documentId:(NSString *)documentId
                             data:(NSDictionary *)data
                       completion:(void (^)(NSError *))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:collection] documentWithPath:documentId] setData:data completion:completion];
}

- (void)getDocumentsWithCollection:(NSString *)collection
                             field:(NSString *)field
                             value:(NSString *)value
                        completion:(void (^)(NSArray<NSDictionary *> *, NSError *))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:collection] queryWhereField:field isEqualTo:value]
        getDocumentsWithCompletion:^(FIRQuerySnapshot *snapshot, NSError *error) {
            if (error) {
                completion(nil, error);
                return;
            }
            NSMutableArray<NSDictionary *> *results = [NSMutableArray array];
            for (FIRQueryDocumentSnapshot *doc in snapshot.documents) {
                [results addObject:doc.data];
            }
            completion(results, nil);
        }];
}

- (void)updateDocumentWithCollection:(NSString *)collection
                          documentId:(NSString *)documentId
                                data:(NSDictionary *)data
                          completion:(void (^)(NSError *))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:collection] documentWithPath:documentId] updateData:data completion:completion];
}

@end
