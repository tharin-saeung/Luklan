#import "FirestoreBridge.h"
#import <FirebaseFirestore/FirebaseFirestore.h>

@implementation FirestoreBridge

+ (void)addMedicineWithId:(NSString *)medicineId
                    name:(NSString *)name
                  dosage:(NSString *)dosage
                    unit:(NSString *)unit
                   times:(NSArray<NSString *> *)times
               startDate:(NSString *)startDate
              expiryDate:(NSString *)expiryDate
                category:(NSString *)category
              mealTiming:(NSString *)mealTiming
       mealTimingMinutes:(int)mealTimingMinutes
                  userId:(NSString *)userId
            takenRecords:(NSDictionary<NSString *, NSNumber *> *)takenRecords
            takenHistory:(NSDictionary<NSString *, NSNumber *> *)takenHistory
               createdAt:(long long)createdAt
                   order:(int)order
              completion:(void (^)(NSString * _Nullable error))completion {
    
    FIRFirestore *db = [FIRFirestore firestore];
    NSDictionary *medicineMap = @{
        @"id": medicineId,
        @"name": name,
        @"dosage": dosage,
        @"unit": unit,
        @"times": times,
        @"startDate": startDate,
        @"expiryDate": expiryDate,
        @"category": category,
        @"mealTiming": mealTiming,
        @"mealTimingMinutes": @(mealTimingMinutes),
        @"userId": userId,
        @"takenRecords": takenRecords,
        @"takenHistory": takenHistory,
        @"createdAt": @(createdAt),
        @"order": @(order)
    };
    
    [[[db collectionWithPath:@"medicines"] documentWithID:medicineId] setData:medicineMap completion:^(NSError * _Nullable error) {
        if (error) {
            completion(error.localizedDescription);
        } else {
            completion(nil);
        }
    }];
}

+ (void)updateMedicineWithId:(NSString *)medicineId
                       name:(NSString *)name
                     dosage:(NSString *)dosage
                       unit:(NSString *)unit
                      times:(NSArray<NSString *> *)times
                  startDate:(NSString *)startDate
                 expiryDate:(NSString *)expiryDate
                   category:(NSString *)category
                 mealTiming:(NSString *)mealTiming
          mealTimingMinutes:(int)mealTimingMinutes
               takenRecords:(NSDictionary<NSString *, NSNumber *> *)takenRecords
               takenHistory:(NSDictionary<NSString *, NSNumber *> *)takenHistory
                  createdAt:(long long)createdAt
                      order:(int)order
                 completion:(void (^)(NSString * _Nullable error))completion {

          FIRFirestore *db = [FIRFirestore firestore];
          NSDictionary *medicineMap = @{
          @"name": name,
          @"dosage": dosage,
          @"unit": unit,
          @"times": times,
          @"startDate": startDate,
          @"expiryDate": expiryDate,
          @"category": category,
          @"mealTiming": mealTiming,
          @"mealTimingMinutes": @(mealTimingMinutes),
          @"takenRecords": takenRecords,
          @"takenHistory": takenHistory,
          @"createdAt": @(createdAt),
          @"order": @(order)
          };
    
    [[[db collectionWithPath:@"medicines"] documentWithID:medicineId] updateData:medicineMap completion:^(NSError * _Nullable error) {
        if (error) {
            completion(error.localizedDescription);
        } else {
            completion(nil);
        }
    }];
}

+ (void)getMedicinesWithUserId:(NSString *)userId
                   completion:(void (^)(NSArray * _Nullable medicines, NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"medicines"] queryWhereField:@"userId" isEqualTo:userId] getDocumentsWithCompletion:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error) {
            completion(nil, error.localizedDescription);
        } else {
            NSMutableArray *medicines = [NSMutableArray array];
            for (FIRQueryDocumentSnapshot *doc in snapshot.documents) {
                NSMutableDictionary *data = [doc.data mutableCopy];
                [data setObject:doc.documentID forKey:@"id"];
                [medicines addObject:data];
            }
            completion(medicines, nil);
        }
    }];
}

+ (void)deleteMedicineWithId:(NSString *)medicineId
                 completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"medicines"] documentWithID:medicineId] deleteDocumentWithCompletion:^(NSError * _Nullable error) {
        if (error) {
            completion(error.localizedDescription);
        } else {
            completion(nil);
        }
    }];
}

// Caretaker Implementation
+ (void)getInviteCodeWithUserId:(NSString *)userId
                     completion:(void (^)(NSString * _Nullable code, NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"users"] documentWithID:userId] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error) {
            completion(nil, error.localizedDescription);
        } else if (snapshot.exists) {
            completion(snapshot.data[@"inviteCode"], nil);
        } else {
            completion(nil, @"User not found");
        }
    }];
}

+ (void)generateInviteCodeWithUserId:(NSString *)userId
                                code:(NSString *)code
                          completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    FIRWriteBatch *batch = [db batch];
    
    FIRDocumentReference *codeRef = [[db collectionWithPath:@"invite_codes"] documentWithID:code];
    [batch setData:@{@"userId": userId, @"createdAt": [FIRTimestamp timestamp]} forDocument:codeRef];
    
    FIRDocumentReference *userRef = [[db collectionWithPath:@"users"] documentWithID:userId];
    [batch setData:@{@"inviteCode": code} forDocument:userRef merge:YES];
    
    [batch commitWithCompletion:^(NSError * _Nullable error) {
        if (error) {
            completion(error.localizedDescription);
        } else {
            completion(nil);
        }
    }];
}

+ (void)connectToPatientWithCaretakerId:(NSString *)caretakerId
                             inviteCode:(NSString *)inviteCode
                             completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"invite_codes"] documentWithID:inviteCode] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable codeSnapshot, NSError * _Nullable error) {
        if (error || !codeSnapshot.exists) {
            completion(@"ไม่พบรหัสเชิญนี้");
            return;
        }
        
        NSString *patientId = codeSnapshot.data[@"userId"];
        if ([patientId isEqualToString:caretakerId]) {
            completion(@"คุณไม่สามารถเป็นผู้ดูแลตัวเองได้");
            return;
        }
        
        FIRWriteBatch *batch = [db batch];
        FIRDocumentReference *patientRef = [[db collectionWithPath:@"users"] documentWithID:patientId];
        [batch updateData:@{@"caretakers": [FIRFieldValue fieldValueForArrayUnion:@[caretakerId]]} forDocument:patientRef];
        
        FIRDocumentReference *caretakerRef = [[db collectionWithPath:@"users"] documentWithID:caretakerId];
        [batch updateData:@{@"patients": [FIRFieldValue fieldValueForArrayUnion:@[patientId]]} forDocument:caretakerRef];
        
        [batch commitWithCompletion:^(NSError * _Nullable error) {
            if (error) {
                completion(error.localizedDescription);
            } else {
                completion(nil);
            }
        }];
    }];
}

+ (void)getUsersWithIds:(NSArray<NSString *> *)userIds
             completion:(void (^)(NSArray * _Nullable users, NSString * _Nullable error))completion {
    if (userIds.count == 0) {
        completion(@[], nil);
        return;
    }
    
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"users"] queryWhereField:FIRFieldPath.documentID inAny:userIds] getDocumentsWithCompletion:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error) {
            completion(nil, error.localizedDescription);
        } else {
            NSMutableArray *results = [NSMutableArray array];
            for (FIRQueryDocumentSnapshot *doc in snapshot.documents) {
                NSMutableDictionary *data = [doc.data mutableCopy];
                [data setObject:doc.documentID forKey:@"id"];
                [results addObject:data];
            }
            completion(results, nil);
        }
    }];
}

// CareGroup Implementation
+ (void)createDefaultGroupWithUser:(NSDictionary *)user
                        completion:(void (^)(NSDictionary * _Nullable group, NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    NSString *groupId = [[db collectionWithPath:@"care_groups"] documentWithID:@""].documentID;
    NSString *userId = user[@"id"];
    NSString *name = [NSString stringWithFormat:@"กลุ่มของ %@", user[@"name"]];
    
    // Generate code
    NSString *letters = @"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    NSMutableString *code = [NSMutableString stringWithCapacity:5];
    for (int i=0; i<5; i++) {
        [code appendFormat: @"%C", [letters characterAtIndex: arc4random_uniform((uint32_t)[letters length])]];
    }

    NSDictionary *groupMap = @{
        @"id": groupId,
        @"name": name,
        @"patientId": userId,
        @"ownerId": userId,
        @"inviteCode": code,
        @"memberIds": @[userId],
        @"createdAt": @((long long)([[NSDate date] timeIntervalSince1970] * 1000))
    };

    FIRWriteBatch *batch = [db batch];
    [batch setData:groupMap forDocument:[[db collectionWithPath:@"care_groups"] documentWithID:groupId]];
    [batch updateData:@{@"groupIds": [FIRFieldValue fieldValueForArrayUnion:@[groupId]]} forDocument:[[db collectionWithPath:@"users"] documentWithID:userId]];

    [batch commitWithCompletion:^(NSError * _Nullable error) {
        if (error) {
            completion(nil, error.localizedDescription);
        } else {
            completion(groupMap, nil);
        }
    }];
}

+ (void)joinGroupWithUserId:(NSString *)userId
                 inviteCode:(NSString *)inviteCode
                 completion:(void (^)(NSDictionary * _Nullable group, NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"care_groups"] queryWhereField:@"inviteCode" isEqualTo:inviteCode] getDocumentsWithCompletion:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error || snapshot.isEmpty) {
            completion(nil, @"ไม่พบกลุ่มด้วยรหัสเชิญนี้");
            return;
        }

        FIRDocumentSnapshot *doc = snapshot.documents.firstObject;
        NSMutableDictionary *group = [doc.data mutableCopy];
        [group setObject:doc.documentID forKey:@"id"];
        
        NSArray *memberIds = group[@"memberIds"];
        if ([memberIds containsObject:userId]) {
            completion(group, nil);
            return;
        }

        FIRWriteBatch *batch = [db batch];
        [batch updateData:@{@"memberIds": [FIRFieldValue fieldValueForArrayUnion:@[userId]]} forDocument:doc.reference];
        [batch updateData:@{@"groupIds": [FIRFieldValue fieldValueForArrayUnion:@[doc.documentID]]} forDocument:[[db collectionWithPath:@"users"] documentWithID:userId]];

        [batch commitWithCompletion:^(NSError * _Nullable error) {
            if (error) {
                completion(nil, error.localizedDescription);
            } else {
                NSMutableArray *newMembers = [memberIds mutableCopy];
                [newMembers addObject:userId];
                [group setObject:newMembers forKey:@"memberIds"];
                completion(group, nil);
            }
        }];
    }];
}

+ (void)getGroupsForUserId:(NSString *)userId
                completion:(void (^)(NSArray * _Nullable groups, NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"users"] documentWithID:userId] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable userSnapshot, NSError * _Nullable error) {
        if (error || !userSnapshot.exists) {
            completion(@[], error.localizedDescription);
            return;
        }
        
        NSArray *groupIds = userSnapshot.data[@"groupIds"];
        if (!groupIds || groupIds.count == 0) {
            completion(@[], nil);
            return;
        }
        
        [[[db collectionWithPath:@"care_groups"] queryWhereField:FIRFieldPath.documentID inAny:groupIds] getDocumentsWithCompletion:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
            if (error) {
                completion(nil, error.localizedDescription);
            } else {
                NSMutableArray *results = [NSMutableArray array];
                for (FIRQueryDocumentSnapshot *doc in snapshot.documents) {
                    NSMutableDictionary *data = [doc.data mutableCopy];
                    [data setObject:doc.documentID forKey:@"id"];
                    [results addObject:data];
                }
                completion(results, nil);
            }
        }];
    }];
}

+ (void)getGroupMembersWithGroupId:(NSString *)groupId
                        completion:(void (^)(NSArray * _Nullable members, NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"care_groups"] documentWithID:groupId] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error || !snapshot.exists) {
            completion(nil, error.localizedDescription);
            return;
        }
        
        NSArray *memberIds = snapshot.data[@"memberIds"];
        [self getUsersWithIds:memberIds completion:completion];
    }];
}

+ (void)deleteGroupWithGroupId:(NSString *)groupId
                    completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"care_groups"] documentWithID:groupId] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error || !snapshot.exists) {
            completion(error.localizedDescription);
            return;
        }
        
        NSArray *memberIds = snapshot.data[@"memberIds"];
        FIRWriteBatch *batch = [db batch];
        for (NSString *mId in memberIds) {
            [batch updateData:@{@"groupIds": [FIRFieldValue fieldValueForArrayRemove:@[groupId]]} forDocument:[[db collectionWithPath:@"users"] documentWithID:mId]];
        }
        [batch deleteDocument:snapshot.reference];
        
        [batch commitWithCompletion:^(NSError * _Nullable error) {
            completion(error ? error.localizedDescription : nil);
        }];
    }];
}

+ (void)getGroupWithGroupId:(NSString *)groupId
                 completion:(void (^)(NSDictionary * _Nullable group, NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"care_groups"] documentWithID:groupId] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error || !snapshot.exists) {
            completion(nil, error.localizedDescription);
        } else {
            NSMutableDictionary *data = [snapshot.data mutableCopy];
            [data setObject:snapshot.documentID forKey:@"id"];
            completion(data, nil);
        }
    }];
}

+ (void)kickMemberWithGroupId:(NSString *)groupId
                       userId:(NSString *)userId
                   completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    FIRWriteBatch *batch = [db batch];
    [batch updateData:@{@"memberIds": [FIRFieldValue fieldValueForArrayRemove:@[userId]]} forDocument:[[db collectionWithPath:@"care_groups"] documentWithID:groupId]];
    [batch updateData:@{@"groupIds": [FIRFieldValue fieldValueForArrayRemove:@[groupId]]} forDocument:[[db collectionWithPath:@"users"] documentWithID:userId]];
    [batch commitWithCompletion:^(NSError * _Nullable error) {
        completion(error ? error.localizedDescription : nil);
    }];
}

+ (void)transferOwnershipWithGroupId:(NSString *)groupId
                          newOwnerId:(NSString *)newOwnerId
                          completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"care_groups"] documentWithID:groupId] updateData:@{@"ownerId": newOwnerId} completion:^(NSError * _Nullable error) {
        completion(error ? error.localizedDescription : nil);
    }];
}

@end
