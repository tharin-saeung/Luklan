#import <Foundation/Foundation.h>

@interface FirestoreBridge : NSObject

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
              completion:(void (^)(NSString * _Nullable error))completion;

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
                 completion:(void (^)(NSString * _Nullable error))completion;

+ (void)getMedicinesWithUserId:(NSString *)userId
                   completion:(void (^)(NSArray * _Nullable medicines, NSString * _Nullable error))completion;

+ (void)deleteMedicineWithId:(NSString *)medicineId
                 completion:(void (^)(NSString * _Nullable error))completion;

// Caretaker Methods
+ (void)getInviteCodeWithUserId:(NSString *)userId
                     completion:(void (^)(NSString * _Nullable code, NSString * _Nullable error))completion;

+ (void)generateInviteCodeWithUserId:(NSString *)userId
                                code:(NSString *)code
                          completion:(void (^)(NSString * _Nullable error))completion;

+ (void)connectToPatientWithCaretakerId:(NSString *)caretakerId
                             inviteCode:(NSString *)inviteCode
                             completion:(void (^)(NSString * _Nullable error))completion;

+ (void)getUsersWithIds:(NSArray<NSString *> *)userIds
             completion:(void (^)(NSArray * _Nullable users, NSString * _Nullable error))completion;

// CareGroup Methods
+ (void)createDefaultGroupWithUser:(NSDictionary *)user
                        completion:(void (^)(NSDictionary * _Nullable group, NSString * _Nullable error))completion;

+ (void)joinGroupWithUserId:(NSString *)userId
                 inviteCode:(NSString *)inviteCode
                 completion:(void (^)(NSDictionary * _Nullable group, NSString * _Nullable error))completion;

+ (void)getGroupsForUserId:(NSString *)userId
                completion:(void (^)(NSArray * _Nullable groups, NSString * _Nullable error))completion;

+ (void)getGroupMembersWithGroupId:(NSString *)groupId
                        completion:(void (^)(NSArray * _Nullable members, NSString * _Nullable error))completion;

+ (void)deleteGroupWithGroupId:(NSString *)groupId
                    completion:(void (^)(NSString * _Nullable error))completion;

+ (void)getGroupWithGroupId:(NSString *)groupId
                 completion:(void (^)(NSDictionary * _Nullable group, NSString * _Nullable error))completion;

+ (void)kickMemberWithGroupId:(NSString *)groupId
                       userId:(NSString *)userId
                   completion:(void (^)(NSString * _Nullable error))completion;

+ (void)transferOwnershipWithGroupId:(NSString *)groupId
                          newOwnerId:(NSString *)newOwnerId
                          completion:(void (^)(NSString * _Nullable error))completion;

@end
