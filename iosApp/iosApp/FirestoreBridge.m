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
        @"takenHistory": takenHistory,
        @"createdAt": @(createdAt),
        @"order": @(order)
    };
    
    NSString *path = [NSString stringWithFormat:@"medicines/%@", medicineId];
    [[db documentWithPath:path] setData:medicineMap completion:^(NSError * _Nullable error) {
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
        @"takenHistory": takenHistory,
        @"createdAt": @(createdAt),
        @"order": @(order)
    };
    
    NSString *path = [NSString stringWithFormat:@"medicines/%@", medicineId];
    [[db documentWithPath:path] updateData:medicineMap completion:^(NSError * _Nullable error) {
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
    NSString *path = [NSString stringWithFormat:@"medicines/%@", medicineId];
    [[db documentWithPath:path] deleteDocumentWithCompletion:^(NSError * _Nullable error) {
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
    NSString *path = [NSString stringWithFormat:@"users/%@", userId];
    [[db documentWithPath:path] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
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
    
    NSString *codePath = [NSString stringWithFormat:@"invite_codes/%@", code];
    FIRDocumentReference *codeRef = [db documentWithPath:codePath];
    [batch setData:@{@"userId": userId, @"createdAt": [NSDate date]} forDocument:codeRef];
    
    NSString *userPath = [NSString stringWithFormat:@"users/%@", userId];
    FIRDocumentReference *userRef = [db documentWithPath:userPath];
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
    NSString *codePath = [NSString stringWithFormat:@"invite_codes/%@", inviteCode];
    [[db documentWithPath:codePath] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable codeSnapshot, NSError * _Nullable error) {
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
        NSString *patientPath = [NSString stringWithFormat:@"users/%@", patientId];
        FIRDocumentReference *patientRef = [db documentWithPath:patientPath];
        [batch setData:@{@"caretakers": [FIRFieldValue fieldValueForArrayUnion:@[caretakerId]]} forDocument:patientRef merge:YES];
        
        NSString *caretakerPath = [NSString stringWithFormat:@"users/%@", caretakerId];
        FIRDocumentReference *caretakerRef = [db documentWithPath:caretakerPath];
        [batch setData:@{@"patients": [FIRFieldValue fieldValueForArrayUnion:@[patientId]]} forDocument:caretakerRef merge:YES];
        
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
    [[[db collectionWithPath:@"users"] queryWhereFieldPath:[FIRFieldPath documentID] in:userIds] getDocumentsWithCompletion:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
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
    FIRDocumentReference *newGroupRef = [[db collectionWithPath:@"care_groups"] documentWithPath:@""];
    NSString *groupId = newGroupRef.documentID;
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
    [batch setData:groupMap forDocument:newGroupRef];
    
    NSString *userPath = [NSString stringWithFormat:@"users/%@", userId];
    [batch setData:@{@"groupIds": [FIRFieldValue fieldValueForArrayUnion:@[groupId]]} forDocument:[db documentWithPath:userPath] merge:YES];

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
        
        NSString *userPath = [NSString stringWithFormat:@"users/%@", userId];
        [batch setData:@{@"groupIds": [FIRFieldValue fieldValueForArrayUnion:@[doc.documentID]]} forDocument:[db documentWithPath:userPath] merge:YES];

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
    NSString *userPath = [NSString stringWithFormat:@"users/%@", userId];
    [[db documentWithPath:userPath] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable userSnapshot, NSError * _Nullable error) {
        if (error || !userSnapshot.exists) {
            completion(@[], error.localizedDescription);
            return;
        }
        
        NSArray *groupIds = userSnapshot.data[@"groupIds"];
        if (!groupIds || groupIds.count == 0) {
            completion(@[], nil);
            return;
        }
        
        [[[db collectionWithPath:@"care_groups"] queryWhereFieldPath:[FIRFieldPath documentID] in:groupIds] getDocumentsWithCompletion:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
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
    NSString *groupPath = [NSString stringWithFormat:@"care_groups/%@", groupId];
    [[db documentWithPath:groupPath] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
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
    NSString *groupPath = [NSString stringWithFormat:@"care_groups/%@", groupId];
    [[db documentWithPath:groupPath] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error || !snapshot.exists) {
            completion(error.localizedDescription);
            return;
        }
        
        NSArray *memberIds = snapshot.data[@"memberIds"];
        FIRWriteBatch *batch = [db batch];
        for (NSString *mId in memberIds) {
            NSString *userPath = [NSString stringWithFormat:@"users/%@", mId];
            [batch updateData:@{@"groupIds": [FIRFieldValue fieldValueForArrayRemove:@[groupId]]} forDocument:[db documentWithPath:userPath]];
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
    NSString *groupPath = [NSString stringWithFormat:@"care_groups/%@", groupId];
    [[db documentWithPath:groupPath] getDocumentWithCompletion:^(FIRDocumentSnapshot * _Nullable snapshot, NSError * _Nullable error) {
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
    NSString *groupPath = [NSString stringWithFormat:@"care_groups/%@", groupId];
    [batch updateData:@{@"memberIds": [FIRFieldValue fieldValueForArrayRemove:@[userId]]} forDocument:[db documentWithPath:groupPath]];
    NSString *userPath = [NSString stringWithFormat:@"users/%@", userId];
    [batch updateData:@{@"groupIds": [FIRFieldValue fieldValueForArrayRemove:@[groupId]]} forDocument:[db documentWithPath:userPath]];
    [batch commitWithCompletion:^(NSError * _Nullable error) {
        completion(error ? error.localizedDescription : nil);
    }];
}

+ (void)transferOwnershipWithGroupId:(NSString *)groupId
                          newOwnerId:(NSString *)newOwnerId
                          completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    NSString *groupPath = [NSString stringWithFormat:@"care_groups/%@", groupId];
    [[db documentWithPath:groupPath] updateData:@{@"ownerId": newOwnerId} completion:^(NSError * _Nullable error) {
        completion(error ? error.localizedDescription : nil);
    }];
}

+ (void)saveUserProfileWithId:(NSString *)userId
                         name:(NSString *)name
                        email:(NSString *)email
                         role:(NSString *)role
                   completion:(void (^)(NSString * _Nullable error))completion {
    FIRFirestore *db = [FIRFirestore firestore];
    NSDictionary *userMap = @{
        @"id": userId,
        @"name": name,
        @"email": email,
        @"role": role,
        @"caretakers": @[],
        @"patients": @[],
        @"groupIds": @[]
    };
    
    NSString *path = [NSString stringWithFormat:@"users/%@", userId];
    [[db documentWithPath:path] setData:userMap merge:YES completion:^(NSError * _Nullable error) {
        if (error) {
            completion(error.localizedDescription);
        } else {
            completion(nil);
        }
    }];
}

@end
