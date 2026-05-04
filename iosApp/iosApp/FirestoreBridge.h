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
           currentAmount:(NSString *)currentAmount
                photoUrl:(NSString *)photoUrl
                  userId:(NSString *)userId
            takenHistory:(NSDictionary<NSString *, NSNumber *> *)takenHistory
             forgotTimes:(int)forgotTimes
   forgotDurationMinutes:(int)forgotDurationMinutes
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
              currentAmount:(NSString *)currentAmount
                   photoUrl:(NSString *)photoUrl
               takenHistory:(NSDictionary<NSString *, NSNumber *> *)takenHistory
                forgotTimes:(int)forgotTimes
      forgotDurationMinutes:(int)forgotDurationMinutes
                  createdAt:(long long)createdAt
                      order:(int)order
                 completion:(void (^)(NSString * _Nullable error))completion;

+ (void)getMedicinesWithUserId:(NSString *)userId
                   completion:(void (^)(NSArray * _Nullable medicines, NSString * _Nullable error))completion;

+ (void)deleteMedicineWithId:(NSString *)medicineId
                 completion:(void (^)(NSString * _Nullable error))completion;

+ (id)listenMedicinesWithUserId:(NSString *)userId
                     completion:(void (^)(NSArray * _Nullable medicines, NSString * _Nullable error))completion;

+ (void)removeListener:(id)listener;

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
+ (void)createGroupWithName:(NSString *)name
                       owner:(NSDictionary *)owner
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

+ (void)updateGroupPhotoWithGroupId:(NSString *)groupId
                           photoUrl:(NSString *)photoUrl
                         completion:(void (^)(NSString * _Nullable error))completion;

+ (void)updateGroupNameWithGroupId:(NSString *)groupId
                              name:(NSString *)name
                        completion:(void (^)(NSString * _Nullable error))completion;

+ (void)saveUserProfileWithId:(NSString *)userId
                         name:(NSString *)name
                        email:(NSString *)email
                         role:(NSString *)role
                   completion:(void (^)(NSString * _Nullable error))completion;

+ (void)updateFcmTokenWithUserId:(NSString *)userId
                           token:(NSString *)token
                      completion:(void (^)(NSString * _Nullable error))completion;

+ (void)updateUserPhotoWithUserId:(NSString *)userId
                         photoUrl:(NSString *)photoUrl
                       completion:(void (^)(NSString * _Nullable error))completion;

+ (void)deleteUserProfileWithId:(NSString *)userId
                         completion:(void (^)(NSString * _Nullable error))completion;

+ (void)sendAlertWithId:(NSString *)alertId
                senderId:(NSString *)senderId
              senderName:(NSString *)senderName
                    type:(NSString *)type
                 message:(NSString *)message
               timestamp:(long long)timestamp
                groupIds:(NSArray<NSString *> *)groupIds
              completion:(void (^)(NSString * _Nullable error))completion;

+ (void)getAlertsForUserId:(NSString *)userId
                completion:(void (^)(NSArray * _Nullable alerts, NSString * _Nullable error))completion;

+ (void)getAlertsBySenderId:(NSString *)senderId
                 completion:(void (^)(NSArray * _Nullable alerts, NSString * _Nullable error))completion;

+ (void)deleteAlertWithId:(NSString *)alertId
               completion:(void (^)(NSString * _Nullable error))completion;

+ (void)deleteAllAlertsForUserId:(NSString *)userId
                        groupIds:(NSArray<NSString *> *)groupIds
                      completion:(void (^)(NSString * _Nullable error))completion;

+ (void)syncAlertWithUserInfo:(NSDictionary *)userInfo
                      alertId:(NSString *)alertId;

+ (void)uploadImageWithData:(NSData *)data
                       path:(NSString *)path
                 completion:(void (^)(NSString * _Nullable url, NSString * _Nullable error))completion;

@end
