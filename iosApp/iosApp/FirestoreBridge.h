#import <Foundation/Foundation.h>

@interface FirestoreBridge : NSObject

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
              completion:(void (^)(NSString * _Nullable error))completion;

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
                 completion:(void (^)(NSString * _Nullable error))completion;

+ (void)getMedicinesWithUserId:(NSString *)userId
                   completion:(void (^)(NSArray * _Nullable medicines, NSString * _Nullable error))completion;

+ (void)deleteMedicineWithId:(NSString *)medicineId
                 completion:(void (^)(NSString * _Nullable error))completion;

@end
