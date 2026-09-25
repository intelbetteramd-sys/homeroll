import CryptoKit
import Foundation
import Security

/// Lowercase hex SHA-256 of the server's own certificate, the first one in the chain.
func leafFingerprint(_ trust: SecTrust) -> String? {
    guard let chain = SecTrustCopyCertificateChain(trust) as? [SecCertificate], let leaf = chain.first else {
        return nil
    }
    let data = SecCertificateCopyData(leaf) as Data
    return SHA256.hash(data: data).map { String(format: "%02x", $0) }.joined()
}

/// The answer to a server trust challenge: go on only with the pinned PC certificate.
/// The host name is not checked: the certificate is self-signed and the pin is stronger.
func answerPinned(
    _ challenge: URLAuthenticationChallenge,
    pins: PinStore
) -> (URLSession.AuthChallengeDisposition, URLCredential?) {
    guard challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust,
          let trust = challenge.protectionSpace.serverTrust else {
        return (.performDefaultHandling, nil)
    }
    guard let fingerprint = leafFingerprint(trust), pins.accept(fingerprint) else {
        return (.cancelAuthenticationChallenge, nil)
    }
    return (.useCredential, URLCredential(trust: trust))
}
