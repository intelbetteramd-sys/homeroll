import Network

/// Finds the PC by Bonjour (`_pixroost._tcp`, announced by the PC) and resolves it to an IPv4 address.
/// The PC answers Bonjour queries only with the firewall rule in place, the same rule the direct path needs.
final class PcBrowser {
    private var browser: NWBrowser?
    private var resolving: [NWConnection] = []

    func start(onFound: @escaping (String, String) -> Void) {
        let browser = NWBrowser(for: .bonjour(type: SpikeConstants.serviceType, domain: nil), using: NWParameters())
        browser.browseResultsChangedHandler = { [weak self] results, _ in
            results.forEach { self?.resolve($0.endpoint, onFound: onFound) }
        }
        browser.start(queue: .main)
        self.browser = browser
    }

    private func resolve(_ endpoint: NWEndpoint, onFound: @escaping (String, String) -> Void) {
        guard case let .service(name, _, _, _) = endpoint else { return }
        let parameters = NWParameters.tcp
        if let ip = parameters.defaultProtocolStack.internetProtocol as? NWProtocolIP.Options {
            ip.version = .v4
        }
        let connection = NWConnection(to: endpoint, using: parameters)
        connection.stateUpdateHandler = { [weak connection] state in
            guard let connection = connection else { return }
            switch state {
            case .ready:
                if case let .hostPort(host, _)? = connection.currentPath?.remoteEndpoint {
                    let address = "\(host)".split(separator: "%").first.map(String.init) ?? "\(host)"
                    onFound(name, address)
                }
                connection.cancel()
            case .failed, .waiting:
                connection.cancel()
            default:
                break
            }
        }
        connection.start(queue: .main)
        resolving.append(connection)
    }
}
