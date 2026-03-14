//
//  FirestoreBridge.h
//  iosApp
//
//  Created by Tharin Saeung on 21/1/2569 BE.
//


#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface FirestoreBridge : NSObject

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
               completion:(void (^)(NSString * _Nullable error))completion;

+ (void)getMedicinesWithUserId:(NSString *)userId
                    completion:(void (^)(NSArray * _Nullable medicines, NSString * _Nullable error))completion;

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
                  completion:(void (^)(NSString * _Nullable error))completion;

+ (void)deleteMedicineWithId:(NSString *)medicineId
                  completion:(void (^)(NSString * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END