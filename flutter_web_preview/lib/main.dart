import 'dart:math' as math;

import 'package:flutter/material.dart';

const ink = Color(0xFF153A34);
const green = Color(0xFF0B8068);
const blue = Color(0xFF317B91);
const canvas = Color(0xFFF3F7F4);
const muted = Color(0xFF6E7E79);
const line = Color(0xFFE0E9E4);

void main() => runApp(const GeoAksesPreview());

class GeoAksesPreview extends StatelessWidget {
  const GeoAksesPreview({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: 'GeoAkses',
    theme: ThemeData(
      useMaterial3: true,
      scaffoldBackgroundColor: canvas,
      colorScheme: ColorScheme.fromSeed(
        seedColor: green,
        brightness: Brightness.light,
      ),
      fontFamily: 'Arial',
    ),
    home: const TransitHome(),
  );
}

class TransitRoute {
  const TransitRoute(
    this.id,
    this.code,
    this.name,
    this.operator,
    this.hours,
    this.fare, {
    this.sourceNote = 'Situs resmi UPTD Angkutan Bandung',
  });
  final String id, code, name, operator, hours, fare, sourceNote;
}

const routes = [
  TransitRoute(
    'TMB-K1',
    'Koridor 1',
    'Cibiru - Cibeureum',
    'TMB',
    '05:30 - 16:30 / 05:30 - 17:30',
    'Rp4.000',
  ),
  TransitRoute(
    'TMB-K2',
    'Koridor 2',
    'Cicaheum - Cibeureum',
    'TMB',
    '05:30 - 17:00 / 05:30 - 17:30',
    'Rp4.000',
  ),
  TransitRoute(
    'TMB-K3',
    'Koridor 3',
    'Cicaheum - Sarijadi',
    'TMB',
    '05:00 - 16:00',
    'Rp4.000',
  ),
  TransitRoute(
    'TMB-K4',
    'Koridor 4',
    'Simpang Antapani - Leuwipanjang',
    'TMB',
    '05:30 - 15:00 / 06:00 - 16:30',
    'Rp4.000',
  ),
  TransitRoute(
    'TMB-K5',
    'Koridor 5',
    'Terminal Antapani - Stasiun Hall',
    'TMB',
    '05:30 - 16:00 / 06:30 - 15:00',
    'Rp4.000',
  ),
  TransitRoute(
    'MJT-1',
    'MJT 1',
    'Leuwipanjang - Soreang',
    'MJT',
    'Belum tersedia',
    'Rp4.900',
    sourceNote: 'Rute Dishub Jabar; tarif dari peta Transport for Bandung',
  ),
  TransitRoute(
    'MJT-2',
    'MJT 2',
    'Alun-Alun Bandung - Kota Baru Parahyangan',
    'MJT',
    'Belum tersedia',
    'Rp4.900',
    sourceNote: 'Rute Dishub Jabar; tarif dari peta Transport for Bandung',
  ),
  TransitRoute(
    'MJT-3',
    'MJT 3',
    'Baleendah - BEC',
    'MJT',
    'Belum tersedia',
    'Rp4.900',
    sourceNote: 'Rute Dishub Jabar; tarif dari peta Transport for Bandung',
  ),
  TransitRoute(
    'MJT-4',
    'MJT 4',
    'Leuwipanjang - Dago',
    'MJT',
    'Belum tersedia',
    'Rp4.900',
    sourceNote: 'Rute Dishub Jabar; tarif dari peta Transport for Bandung',
  ),
  TransitRoute(
    'MJT-5',
    'MJT 5',
    'Dipatiukur - Jatinangor',
    'MJT',
    'Belum tersedia',
    'Rp4.900',
    sourceNote: 'Rute Dishub Jabar; tarif dari peta Transport for Bandung',
  ),
  TransitRoute(
    'MJT-6',
    'MJT 6',
    'Leuwipanjang - Majalaya',
    'MJT',
    'Belum tersedia',
    'Rp4.900',
    sourceNote: 'Rute Dishub Jabar; tarif dari peta Transport for Bandung',
  ),
];

class TransitHome extends StatefulWidget {
  const TransitHome({super.key});
  @override
  State<TransitHome> createState() => _TransitHomeState();
}

class _TransitHomeState extends State<TransitHome> {
  String filter = 'Semua', query = '', selectedId = 'TMB-K1';

  TransitRoute get selected => routes.firstWhere(
    (route) => route.id == selectedId,
    orElse: () => routes.first,
  );

  List<TransitRoute> get visibleRoutes => routes.where((route) {
    final matchesOperator = filter == 'Semua' || route.operator == filter;
    final search = query.toLowerCase();
    return matchesOperator &&
        (search.isEmpty ||
            route.name.toLowerCase().contains(search) ||
            route.code.toLowerCase().contains(search));
  }).toList();

  @override
  Widget build(BuildContext context) => Scaffold(
    body: SafeArea(
      child: Column(
        children: [
          const TopBar(),
          Expanded(
            child: LayoutBuilder(
              builder: (context, constraints) {
                final panel = RoutePanel(
                  filter: filter,
                  query: query,
                  selectedId: selectedId,
                  visibleRoutes: visibleRoutes,
                  onFilter: (value) => setState(() => filter = value),
                  onQuery: (value) => setState(() => query = value),
                  onSelect: (value) => setState(() => selectedId = value),
                );
                if (constraints.maxWidth >= 980) {
                  return Row(
                    children: [
                      SizedBox(width: 370, child: panel),
                      Expanded(child: MapPanel(selected: selected)),
                    ],
                  );
                }
                return SingleChildScrollView(
                  child: Column(
                    children: [
                      SizedBox(
                        height: constraints.maxWidth < 600 ? 300 : 420,
                        child: MapPanel(selected: selected),
                      ),
                      SizedBox(height: 560, child: panel),
                    ],
                  ),
                );
              },
            ),
          ),
        ],
      ),
    ),
  );
}

class TopBar extends StatelessWidget {
  const TopBar({super.key});
  @override
  Widget build(BuildContext context) => LayoutBuilder(
    builder: (context, constraints) {
      final compact = constraints.maxWidth < 600;
      return Container(
        height: compact ? 62 : 76,
        padding: EdgeInsets.symmetric(horizontal: compact ? 14 : 28),
        decoration: const BoxDecoration(
          color: Colors.white,
          border: Border(bottom: BorderSide(color: line)),
        ),
        child: Row(
          children: [
            Container(
              width: compact ? 36 : 42,
              height: compact ? 36 : 42,
              decoration: BoxDecoration(
                color: green,
                borderRadius: BorderRadius.circular(13),
              ),
              child: Icon(
                Icons.alt_route_rounded,
                color: Colors.white,
                size: compact ? 21 : 25,
              ),
            ),
            SizedBox(width: compact ? 8 : 11),
            Text(
              'GeoAkses',
              style: TextStyle(
                fontSize: compact ? 18 : 21,
                fontWeight: FontWeight.w800,
                color: ink,
                letterSpacing: -0.6,
              ),
            ),
            if (!compact) ...[
              const SizedBox(width: 12),
              Container(width: 1, height: 28, color: line),
              const SizedBox(width: 12),
              const Text(
                'Transit Bandung Raya',
                style: TextStyle(
                  fontSize: 13,
                  color: muted,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
            const Spacer(),
            _StatusPill(
              label: 'Posisi bus belum tersedia',
              icon: Icons.location_searching_rounded,
              compact: compact,
            ),
            SizedBox(width: compact ? 8 : 14),
            Container(
              width: compact ? 34 : 38,
              height: compact ? 34 : 38,
              decoration: BoxDecoration(
                color: canvas,
                borderRadius: BorderRadius.circular(13),
              ),
              child: Icon(
                Icons.person_outline_rounded,
                color: ink,
                size: compact ? 19 : 21,
              ),
            ),
          ],
        ),
      );
    },
  );
}

class RoutePanel extends StatelessWidget {
  const RoutePanel({
    required this.filter,
    required this.query,
    required this.selectedId,
    required this.visibleRoutes,
    required this.onFilter,
    required this.onQuery,
    required this.onSelect,
    super.key,
  });
  final String filter, query, selectedId;
  final List<TransitRoute> visibleRoutes;
  final ValueChanged<String> onFilter, onQuery, onSelect;

  @override
  Widget build(BuildContext context) => Container(
    decoration: const BoxDecoration(
      color: Colors.white,
      border: Border(right: BorderSide(color: line)),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(24, 24, 24, 18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Expanded(
                    child: Text(
                      'Jelajahi koridor',
                      style: TextStyle(
                        fontSize: 25,
                        fontWeight: FontWeight.w800,
                        color: ink,
                        letterSpacing: -0.8,
                      ),
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 10,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      color: const Color(0xFFEAF4EF),
                      borderRadius: BorderRadius.circular(9),
                    ),
                    child: Text(
                      '${visibleRoutes.length} rute',
                      style: const TextStyle(
                        color: green,
                        fontWeight: FontWeight.w700,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              const Text(
                'Pilih layanan untuk melihat informasi koridor.',
                style: TextStyle(color: muted, fontSize: 13),
              ),
              const SizedBox(height: 18),
              TextField(
                onChanged: onQuery,
                decoration: InputDecoration(
                  hintText: 'Cari tujuan atau koridor',
                  hintStyle: const TextStyle(
                    color: Color(0xFF8A9994),
                    fontSize: 13,
                  ),
                  prefixIcon: const Icon(
                    Icons.search_rounded,
                    color: green,
                    size: 21,
                  ),
                  filled: true,
                  fillColor: canvas,
                  contentPadding: const EdgeInsets.symmetric(vertical: 13),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                    borderSide: BorderSide.none,
                  ),
                ),
              ),
              const SizedBox(height: 14),
              Row(
                children: ['Semua', 'TMB', 'MJT'].map((item) {
                  final active = filter == item;
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: InkWell(
                      borderRadius: BorderRadius.circular(10),
                      onTap: () => onFilter(item),
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 160),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 15,
                          vertical: 9,
                        ),
                        decoration: BoxDecoration(
                          color: active ? ink : Colors.white,
                          border: Border.all(color: active ? ink : line),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(
                          item,
                          style: TextStyle(
                            color: active ? Colors.white : muted,
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
            ],
          ),
        ),
        const Divider(height: 1, color: line),
        Expanded(
          child: ListView.separated(
            padding: const EdgeInsets.fromLTRB(16, 14, 16, 20),
            itemCount: visibleRoutes.length,
            separatorBuilder: (_, _) => const SizedBox(height: 9),
            itemBuilder: (context, index) {
              final route = visibleRoutes[index];
              return RouteTile(
                route: route,
                selected: route.id == selectedId,
                onTap: () => onSelect(route.id),
              );
            },
          ),
        ),
        const Padding(
          padding: EdgeInsets.fromLTRB(22, 0, 22, 18),
          child: Row(
            children: [
              Icon(Icons.info_outline_rounded, size: 15, color: muted),
              SizedBox(width: 7),
              Expanded(
                child: Text(
                  'Tarif adalah tarif dasar. Jam mengikuti sumber yang tersedia.',
                  style: TextStyle(color: muted, fontSize: 11, height: 1.35),
                ),
              ),
            ],
          ),
        ),
      ],
    ),
  );
}

class RouteTile extends StatelessWidget {
  const RouteTile({
    required this.route,
    required this.selected,
    required this.onTap,
    super.key,
  });
  final TransitRoute route;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final operatorColor = route.operator == 'TMB' ? green : blue;
    return Material(
      color: selected ? const Color(0xFFF0F7F3) : Colors.white,
      borderRadius: BorderRadius.circular(15),
      child: InkWell(
        borderRadius: BorderRadius.circular(15),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(15),
            border: Border.all(
              color: selected ? const Color(0xFF81B8A7) : line,
              width: selected ? 1.4 : 1,
            ),
          ),
          child: Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: operatorColor.withValues(alpha: 0.11),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  Icons.directions_bus_filled_rounded,
                  size: 21,
                  color: operatorColor,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(
                          route.code,
                          style: TextStyle(
                            color: operatorColor,
                            fontWeight: FontWeight.w800,
                            fontSize: 11,
                          ),
                        ),
                        const SizedBox(width: 7),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 6,
                            vertical: 3,
                          ),
                          decoration: BoxDecoration(
                            color: operatorColor.withValues(alpha: 0.09),
                            borderRadius: BorderRadius.circular(5),
                          ),
                          child: Text(
                            route.operator,
                            style: TextStyle(
                              color: operatorColor,
                              fontWeight: FontWeight.w800,
                              fontSize: 9,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      route.name,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: ink,
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      route.hours,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(color: muted, fontSize: 10),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              Text(
                route.fare,
                style: const TextStyle(
                  color: ink,
                  fontSize: 11,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class MapPanel extends StatelessWidget {
  const MapPanel({required this.selected, super.key});
  final TransitRoute selected;

  @override
  Widget build(BuildContext context) => LayoutBuilder(
    builder: (context, constraints) {
      final compact = constraints.maxWidth < 600;
      return Stack(
        fit: StackFit.expand,
        children: [
          const BandungMap(),
          Positioned(
            top: compact ? 12 : 22,
            left: compact ? 12 : 24,
            child: _MapLabel(
              icon: Icons.map_outlined,
              title: compact ? 'Bandung' : 'Peta Bandung',
              subtitle: 'Jalan dan kawasan sekitar',
              compact: compact,
            ),
          ),
          Positioned(
            top: compact ? 12 : 22,
            right: compact ? 12 : 24,
            child: Container(
              constraints: BoxConstraints(maxWidth: compact ? 145 : 260),
              padding: EdgeInsets.all(compact ? 9 : 14),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                boxShadow: const [
                  BoxShadow(
                    color: Color(0x1A153A34),
                    blurRadius: 24,
                    offset: Offset(0, 8),
                  ),
                ],
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _Dot(color: Color(0xFFD59047)),
                  SizedBox(width: compact ? 6 : 9),
                  Flexible(
                    child: compact
                        ? const Text(
                            'Live belum tersedia',
                            style: TextStyle(
                              color: muted,
                              fontSize: 9,
                              fontWeight: FontWeight.w700,
                            ),
                          )
                        : const Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Armada live',
                                style: TextStyle(
                                  color: ink,
                                  fontWeight: FontWeight.w800,
                                  fontSize: 12,
                                ),
                              ),
                              SizedBox(height: 3),
                              Text(
                                'Feed posisi belum tersedia',
                                style: TextStyle(color: muted, fontSize: 10),
                              ),
                            ],
                          ),
                  ),
                ],
              ),
            ),
          ),
          Positioned(
            left: compact ? 12 : 24,
            bottom: compact ? 42 : 26,
            child: ConstrainedBox(
              constraints: BoxConstraints(
                maxWidth: math.min(
                  constraints.maxWidth - (compact ? 24 : 48),
                  compact ? 245 : 390,
                ),
              ),
              child: Container(
                padding: EdgeInsets.all(compact ? 12 : 17),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(17),
                  boxShadow: const [
                    BoxShadow(
                      color: Color(0x1A153A34),
                      blurRadius: 22,
                      offset: Offset(0, 7),
                    ),
                  ],
                ),
                child: Row(
                  children: [
                    Container(
                      width: compact ? 34 : 42,
                      height: compact ? 34 : 42,
                      decoration: BoxDecoration(
                        color: const Color(0xFFE8F3EE),
                        borderRadius: BorderRadius.circular(13),
                      ),
                      child: Icon(
                        Icons.alt_route_rounded,
                        color: green,
                        size: compact ? 19 : 23,
                      ),
                    ),
                    SizedBox(width: compact ? 9 : 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '${selected.code} / ${selected.operator}',
                            style: const TextStyle(
                              color: green,
                              fontSize: 10,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                          const SizedBox(height: 3),
                          Text(
                            selected.name,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              color: ink,
                              fontSize: 13,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                          if (!compact) ...[
                            const SizedBox(height: 4),
                            Text(
                              selected.sourceNote,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: muted,
                                height: 1.3,
                                fontSize: 10,
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                    if (!compact) ...[
                      const SizedBox(width: 10),
                      const Icon(
                        Icons.north_east_rounded,
                        size: 18,
                        color: muted,
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
          Positioned(
            right: compact ? 9 : 15,
            bottom: compact ? 8 : 15,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 4),
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.94),
                borderRadius: BorderRadius.circular(6),
              ),
              child: const Text(
                'Map: OpenStreetMap',
                style: TextStyle(
                  color: Color(0xFF344942),
                  fontSize: 8,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
        ],
      );
    },
  );
}

class BandungMap extends StatelessWidget {
  const BandungMap({super.key});
  @override
  Widget build(BuildContext context) {
    final tile = _tileFor(-6.917464, 107.619123, 14);
    return LayoutBuilder(
      builder: (context, constraints) {
        final width = math.max(constraints.maxWidth, 768.0);
        final height = math.max(constraints.maxHeight, 768.0);
        return ClipRect(
          child: FittedBox(
            fit: BoxFit.cover,
            child: SizedBox(
              width: width,
              height: height,
              child: GridView.builder(
                physics: const NeverScrollableScrollPhysics(),
                padding: EdgeInsets.zero,
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                ),
                itemCount: 9,
                itemBuilder: (context, index) {
                  final x = tile.$1 + (index % 3) - 1;
                  final y = tile.$2 + (index ~/ 3) - 1;
                  return Image.network(
                    'https://tile.openstreetmap.org/14/$x/$y.png',
                    fit: BoxFit.cover,
                    errorBuilder: (_, _, _) => const _MapFallback(),
                  );
                },
              ),
            ),
          ),
        );
      },
    );
  }

  (int, int) _tileFor(double latitude, double longitude, int zoom) {
    final n = math.pow(2, zoom);
    final x = ((longitude + 180) / 360 * n).floor();
    final latRad = latitude * math.pi / 180;
    final y =
        ((1 - math.log(math.tan(latRad) + 1 / math.cos(latRad)) / math.pi) /
                2 *
                n)
            .floor();
    return (x, y);
  }
}

class _MapFallback extends StatelessWidget {
  const _MapFallback();
  @override
  Widget build(BuildContext context) => CustomPaint(
    painter: _FallbackPainter(),
    child: const Center(
      child: Icon(Icons.public_rounded, color: Color(0xFF9CB8AC), size: 30),
    ),
  );
}

class _FallbackPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawColor(const Color(0xFFE8EFEA), BlendMode.src);
    final road = Paint()
      ..color = const Color(0xFFD1DDD5)
      ..strokeWidth = 2;
    for (var i = 1; i < 5; i++) {
      final offset = size.width * i / 5;
      canvas.drawLine(
        Offset(offset, 0),
        Offset(offset - 25, size.height),
        road,
      );
      canvas.drawLine(Offset(0, offset), Offset(size.width, offset + 18), road);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _MapLabel extends StatelessWidget {
  const _MapLabel({
    required this.icon,
    required this.title,
    required this.subtitle,
    this.compact = false,
  });
  final IconData icon;
  final String title, subtitle;
  final bool compact;

  @override
  Widget build(BuildContext context) => Container(
    padding: EdgeInsets.symmetric(
      horizontal: compact ? 10 : 15,
      vertical: compact ? 9 : 12,
    ),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(15),
      boxShadow: const [
        BoxShadow(
          color: Color(0x1A153A34),
          blurRadius: 22,
          offset: Offset(0, 7),
        ),
      ],
    ),
    child: Row(
      children: [
        Icon(icon, color: green, size: compact ? 16 : 20),
        SizedBox(width: compact ? 7 : 10),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: TextStyle(
                color: ink,
                fontWeight: FontWeight.w800,
                fontSize: compact ? 10 : 12,
              ),
            ),
            if (!compact) ...[
              const SizedBox(height: 2),
              Text(
                subtitle,
                style: const TextStyle(color: muted, fontSize: 10),
              ),
            ],
          ],
        ),
      ],
    ),
  );
}

class _StatusPill extends StatelessWidget {
  const _StatusPill({
    required this.label,
    required this.icon,
    this.compact = false,
  });
  final String label;
  final IconData icon;
  final bool compact;

  @override
  Widget build(BuildContext context) => Container(
    padding: EdgeInsets.symmetric(horizontal: compact ? 9 : 12, vertical: 9),
    decoration: BoxDecoration(
      color: const Color(0xFFFFF4E8),
      borderRadius: BorderRadius.circular(10),
    ),
    child: Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 15, color: const Color(0xFFB56E27)),
        if (!compact) ...[
          const SizedBox(width: 7),
          Text(
            label,
            style: const TextStyle(
              color: Color(0xFF835625),
              fontSize: 11,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ],
    ),
  );
}

class _Dot extends StatelessWidget {
  const _Dot({required this.color});
  final Color color;
  @override
  Widget build(BuildContext context) => Container(
    width: 9,
    height: 9,
    decoration: BoxDecoration(color: color, shape: BoxShape.circle),
  );
}
