import Foundation

enum SpikeError: LocalizedError {
    case badAddress(String)
    case status(Int)

    var errorDescription: String? {
        switch self {
        case .badAddress(let host): return "неверный адрес ПК: \(host)"
        case .status(let code): return "ПК ответил \(code)"
        }
    }
}
