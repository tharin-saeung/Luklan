//
//  FirestoreBridge.m
//  iosApp
//
//  Created by Tharin Saeung on 21/1/2569 BE.
//


#import "FirestoreBridge.h"
@import FirebaseFirestore;

@implementation FirestoreBridge

+ (void)addMedicineWithId:(NSString *)medicineId
                     name:(NSString *)name
              description:(NSString *)description
                   dosage:(NSString *)dosage
                     time:(NSString *)time
                frequency:(NSString *)frequency
                 quantity:(NSInteger)quantity
                     unit:(NSString *)unit
               expiryDate:(NSString *)expiryDate
                 category:(NSString *)category
     storageInstructions:(NSString *)storageInstructions
                    notes:(NSString *)notes
                   userId:(NSString *)userId
                    taken:(BOOL)taken
                createdAt:(long long)createdAt
               completion:(void (^)(NSString * _Nullable))completion {
    
    FIRFirestore *db = [FIRFirestore firestore];
    NSDictionary *data = @{
        @"id": medicineId,
        @"name": name,
        @"description": description,
        @"dosage": dosage,
        @"time": time,
        @"frequency": frequency,
        @"quantity": @(quantity),
        @"unit": unit,
        @"expiryDate": expiryDate,
        @"category": category,
        @"storageInstructions": storageInstructions,
        @"notes": notes,
        @"userId": userId,
        @"taken": @(taken),
        @"createdAt": @(createdAt)
    };
    
    [[[db collectionWithPath:@"medicines"] documentWithPath:medicineId] setData:data completion:^(NSError * _Nullable error) {
        completion(error.localizedDescription);
    }];
}

+ (void)getMedicinesWithUserId:(NSString *)userId
                    completion:(void (^)(NSArray * _Nullable, NSString * _Nullable))completion {
    
    FIRFirestore *db = [FIRFirestore firestore];
    FIRQuery *query = [[db collectionWithPath:@"medicines"] queryWhereField:@"userId" isEqualTo:userId];
    
    [query getDocumentsWithCompletion:^(FIRQuerySnapshot * _Nullable snapshot, NSError * _Nullable error) {
        if (error) {
            completion(nil, error.localizedDescription);
            return;
        }
        
        NSMutableArray *medicines = [NSMutableArray array];
        for (FIRDocumentSnapshot *doc in snapshot.documents) {
            [medicines addObject:doc.data];
        }
        completion(medicines, nil);
    }];
}

+ (void)updateMedicineWithId:(NSString *)medicineId
                        name:(NSString *)name
                 description:(NSString *)description
                      dosage:(NSString *)dosage
                        time:(NSString *)time
                   frequency:(NSString *)frequency
                    quantity:(NSInteger)quantity
                        unit:(NSString *)unit
                  expiryDate:(NSString *)expiryDate
                    category:(NSString *)category
        storageInstructions:(NSString *)storageInstructions
                       notes:(NSString *)notes
                       taken:(BOOL)taken
                   createdAt:(long long)createdAt
                  completion:(void (^)(NSString * _Nullable))completion {
    
    FIRFirestore *db = [FIRFirestore firestore];
    NSDictionary *data = @{
        @"name": name,
        @"description": description,
        @"dosage": dosage,
        @"time": time,
        @"frequency": frequency,
        @"quantity": @(quantity),
        @"unit": unit,
        @"expiryDate": expiryDate,
        @"category": category,
        @"storageInstructions": storageInstructions,
        @"notes": notes,
        @"taken": @(taken),
        @"createdAt": @(createdAt)
    };
    
    [[[db collectionWithPath:@"medicines"] documentWithPath:medicineId] updateData:data completion:^(NSError * _Nullable error) {
        completion(error.localizedDescription);
    }];
}

+ (void)deleteMedicineWithId:(NSString *)medicineId
                  completion:(void (^)(NSString * _Nullable))completion {
    
    FIRFirestore *db = [FIRFirestore firestore];
    [[[db collectionWithPath:@"medicines"] documentWithPath:medicineId] deleteDocumentWithCompletion:^(NSError * _Nullable error) {
        completion(error.localizedDescription);
    }];
}

@end