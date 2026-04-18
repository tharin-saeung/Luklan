#import "FirestoreBridge.h"
#import <FirebaseFirestore/FirebaseFirestore.h>

@implementation FirestoreBridge

+ (void)addMedicineWithId:(NSString *)medicineId
                    name:(NSString *)name
             description:(NSString *)description
                  dosage:(NSString *)dosage
                    time:(NSString *)time
               frequency:(NSString *)frequency
                quantity:(long)quantity
                    unit:(NSString *)unit
               startDate:(NSString *)startDate
              expiryDate:(NSString *)expiryDate
                category:(NSString *)category
              mealTiming:(NSString *)mealTiming
     storageInstructions:(NSString *)storageInstructions
                   notes:(NSString *)notes
                   times:(NSArray<NSString *> *)times
                  userId:(NSString *)userId
                   taken:(BOOL)taken
            takenRecords:(NSDictionary<NSString *, NSNumber *> *)takenRecords
               createdAt:(long long)createdAt
                   order:(int)order
              completion:(void (^)(NSString * _Nullable error))completion {
    
    FIRFirestore *db = [FIRFirestore firestore];
    NSDictionary *medicineMap = @{
        @"id": medicineId,
        @"name": name,
        @"description": description,
        @"dosage": dosage,
        @"time": time,
        @"times": times,
        @"frequency": frequency,
        @"timeUnit": @"วัน",
        @"frequencyCount": @(1),
        @"amountPerDose": dosage,
        @"quantity": @(quantity),
        @"unit": unit,
        @"startDate": startDate,
        @"expiryDate": expiryDate,
        @"category": category,
        @"mealTiming": mealTiming,
        @"storageInstructions": storageInstructions,
        @"notes": notes,
        @"userId": userId,
        @"taken": @(taken),
        @"takenRecords": takenRecords,
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
                description:(NSString *)description
                     dosage:(NSString *)dosage
                       time:(NSString *)time
                  frequency:(NSString *)frequency
                   quantity:(long)quantity
                       unit:(NSString *)unit
                  startDate:(NSString *)startDate
                 expiryDate:(NSString *)expiryDate
                   category:(NSString *)category
                 mealTiming:(NSString *)mealTiming
        storageInstructions:(NSString *)storageInstructions
                      notes:(NSString *)notes
                      times:(NSArray<NSString *> *)times
                      taken:(BOOL)taken
               takenRecords:(NSDictionary<NSString *, NSNumber *> *)takenRecords
                  createdAt:(long long)createdAt
                      order:(int)order
                 completion:(void (^)(NSString * _Nullable error))completion {
    
    FIRFirestore *db = [FIRFirestore firestore];
    NSDictionary *medicineMap = @{
        @"id": medicineId,
        @"name": name,
        @"description": description,
        @"dosage": dosage,
        @"time": time,
        @"times": times,
        @"frequency": frequency,
        @"timeUnit": @"วัน",
        @"frequencyCount": @(1),
        @"amountPerDose": dosage,
        @"quantity": @(quantity),
        @"unit": unit,
        @"startDate": startDate,
        @"expiryDate": expiryDate,
        @"category": category,
        @"mealTiming": mealTiming,
        @"storageInstructions": storageInstructions,
        @"notes": notes,
        @"taken": @(taken),
        @"takenRecords": takenRecords,
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

@end
