import Foundation
import FirebaseFirestore
import composeApp

@objc public class FirestoreManager: NSObject {
    private let db = Firestore.firestore()
    
    @objc public static let shared = FirestoreManager()
    
    @objc public func addMedicine(
        id: String,
        name: String,
        description: String,
        time: String,
        userId: String,
        taken: Bool,
        completion: @escaping (String?) -> Void
    ) {
        let data: [String: Any] = [
            "id": id,
            "name": name,
            "description": description,
            "time": time,
            "userId": userId,
            "taken": taken
        ]
        
        db.collection("medicines").document(id).setData(data) { error in
            completion(error?.localizedDescription)
        }
    }
    
    @objc public func getMedicines(
        userId: String,
        completion: @escaping ([NSDictionary]?, String?) -> Void
    ) {
        db.collection("medicines")
            .whereField("userId", isEqualTo: userId)
            .getDocuments { snapshot, error in
                if let error = error {
                    completion(nil, error.localizedDescription)
                    return
                }
                
                let medicines = snapshot?.documents.compactMap { doc -> NSDictionary? in
                    let data = doc.data()
                    return [
                        "id": data["id"] as? String ?? "",
                        "name": data["name"] as? String ?? "",
                        "description": data["description"] as? String ?? "",
                        "time": data["time"] as? String ?? "",
                        "userId": data["userId"] as? String ?? "",
                        "taken": data["taken"] as? Bool ?? false
                    ] as NSDictionary
                }
                completion(medicines, nil)
            }
    }
    
    @objc public func updateMedicine(
        id: String,
        name: String,
        description: String,
        time: String,
        taken: Bool,
        completion: @escaping (String?) -> Void
    ) {
        let data: [String: Any] = [
            "name": name,
            "description": description,
            "time": time,
            "taken": taken
        ]
        
        db.collection("medicines").document(id).updateData(data) { error in
            completion(error?.localizedDescription)
        }
    }
    
    @objc public func deleteMedicine(
        id: String,
        completion: @escaping (String?) -> Void
    ) {
        db.collection("medicines").document(id).delete { error in
            completion(error?.localizedDescription)
        }
    }
}
