"use client";

import { useState, useEffect, useMemo } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";
import Image from "next/image";
import { 
  Search, 
  SlidersHorizontal, 
  X, 
  ChevronDown,
  ArrowLeft,
  Car,
  Filter
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { VehicleCard } from "@/components/vehicle-card";
import { 
  allVehicles, 
  brands, 
  locations, 
  years, 
  fuelTypes, 
  transmissions 
} from "@/lib/vehicles";

type FilterType = "venta" | "alquiler";

export default function VehiculosPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  
  const [search, setSearch] = useState("");
  const [type, setType] = useState<FilterType>("venta");
  const [selectedBrand, setSelectedBrand] = useState<string>("todos");
  const [selectedLocation, setSelectedLocation] = useState<string>("todos");
  const [selectedYear, setSelectedYear] = useState<string>("todos");
  const [selectedFuel, setSelectedFuel] = useState<string>("todos");
  const [selectedTransmission, setSelectedTransmission] = useState<string>("todos");
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 100000000]);
  const [showFilters, setShowFilters] = useState(false);
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    const tipo = searchParams.get("tipo");
    if (tipo === "venta" || tipo === "alquiler") {
      setType(tipo);
    } else {
      setType("venta");
      router.replace("/vehiculos?tipo=venta");
    }
    setIsLoaded(true);
  }, [searchParams, router]);

  const filteredVehicles = useMemo(() => {
    return allVehicles.filter((vehicle) => {
      // Type filter
      if (vehicle.type !== type) return false;
      
      // Search filter
      if (search) {
        const searchLower = search.toLowerCase();
        const matchesSearch = 
          vehicle.name.toLowerCase().includes(searchLower) ||
          vehicle.brand.toLowerCase().includes(searchLower) ||
          vehicle.model.toLowerCase().includes(searchLower);
        if (!matchesSearch) return false;
      }
      
      // Brand filter
      if (selectedBrand !== "todos" && vehicle.brand !== selectedBrand) return false;
      
      // Location filter
      if (selectedLocation !== "todos" && vehicle.location !== selectedLocation) return false;
      
      // Year filter
      if (selectedYear !== "todos" && vehicle.year.toString() !== selectedYear) return false;
      
      // Fuel filter
      if (selectedFuel !== "todos" && vehicle.fuel !== selectedFuel) return false;
      
      // Transmission filter
      if (selectedTransmission !== "todos" && vehicle.transmission !== selectedTransmission) return false;
      
      // Price filter
      if (vehicle.priceNumber < priceRange[0] || vehicle.priceNumber > priceRange[1]) return false;
      
      return true;
    });
  }, [type, search, selectedBrand, selectedLocation, selectedYear, selectedFuel, selectedTransmission, priceRange]);

  const clearFilters = () => {
    setSearch("");
    setType("venta");
    setSelectedBrand("todos");
    setSelectedLocation("todos");
    setSelectedYear("todos");
    setSelectedFuel("todos");
    setSelectedTransmission("todos");
    setPriceRange([0, 100000000]);
    router.push("/vehiculos?tipo=venta");
  };

  const hasActiveFilters = 
    selectedBrand !== "todos" || 
    selectedLocation !== "todos" || 
    selectedYear !== "todos" ||
    selectedFuel !== "todos" ||
    selectedTransmission !== "todos" ||
    search !== "";

  return (
    <main className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-background/80 backdrop-blur-md border-b border-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <Link 
              href="/" 
              className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              <span className="hidden sm:inline">Volver al inicio</span>
            </Link>
            
            <Link href="/" className="flex items-center gap-2">
              <Image
                src="/Alsina.png"
                alt="Alsina"
                width={40}
                height={40}
                className="object-contain rounded-lg"
              />
            </Link>

            <div className="w-24" />
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Title */}
        <div className={`mb-8 transition-all duration-700 ${isLoaded ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}`}>
          <h1 className="text-4xl sm:text-5xl font-black text-foreground">
            {type === "venta" ? "VEHÍCULOS EN VENTA" : "FLOTA DE ALQUILER"}
          </h1>
          <p className="text-muted-foreground mt-2">
            {filteredVehicles.length} {filteredVehicles.length === 1 ? "vehículo encontrado" : "vehículos encontrados"}
          </p>
        </div>

        {/* Search and Filter Bar */}
        <div className={`flex flex-col sm:flex-row gap-4 mb-8 transition-all duration-700 delay-100 ${isLoaded ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}`}>
          {/* Search */}
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <Input
              type="text"
              placeholder="Buscar por marca, modelo..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-10 bg-card border-border h-12"
            />
            {search && (
              <button
                onClick={() => setSearch("")}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Type Tabs */}
          <div className="flex bg-card border border-border rounded-lg p-1">
            {[
              { value: "venta", label: "Venta" },
              { value: "alquiler", label: "Alquiler" },
            ].map((tab) => (
              <button
                key={tab.value}
                onClick={() => setType(tab.value as FilterType)}
                className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${
                  type === tab.value
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Filter Toggle */}
          <Button
            variant="outline"
            onClick={() => setShowFilters(!showFilters)}
            className={`h-12 gap-2 ${showFilters ? "bg-primary text-primary-foreground border-primary" : ""}`}
          >
            <SlidersHorizontal className="w-4 h-4" />
            Filtros
            {hasActiveFilters && (
              <span className="w-2 h-2 bg-primary rounded-full" />
            )}
          </Button>
        </div>

        {/* Filters Panel */}
        {showFilters && (
          <div className={`bg-zinc-800 border border-zinc-700 rounded-xl p-6 mb-8 transition-all duration-300 ${showFilters ? "opacity-100 translate-y-0" : "opacity-0 -translate-y-4"}`}>
            <div className="flex items-center justify-between mb-6">
              <h3 className="font-bold text-white flex items-center gap-2 text-lg">
                <Filter className="w-5 h-5 text-white" />
                Filtros avanzados
              </h3>
              {hasActiveFilters && (
                <button
                  onClick={clearFilters}
                  className="text-sm text-primary hover:underline"
                >
                  Limpiar filtros
                </button>
              )}
            </div>

            <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
              {/* Brand */}
              <div>
                <label className="block text-sm text-white/90 mb-2 font-medium">Marca</label>
                <div className="relative">
                  <select
                    value={selectedBrand}
                    onChange={(e) => setSelectedBrand(e.target.value)}
                    className="w-full h-10 px-3 bg-zinc-700/80 border border-zinc-600 rounded-lg text-foreground appearance-none cursor-pointer"
                  >
                    <option value="todos">Todas</option>
                    {brands.map((brand) => (
                      <option key={brand} value={brand}>{brand}</option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
                </div>
              </div>

              {/* Location */}
              <div>
                <label className="block text-sm text-primary-foreground/80 mb-2 font-medium">Ubicación</label>
                <div className="relative">
                  <select
                    value={selectedLocation}
                    onChange={(e) => setSelectedLocation(e.target.value)}
                    className="w-full h-10 px-3 bg-zinc-700/80 border border-zinc-600 rounded-lg text-foreground appearance-none cursor-pointer"
                  >
                    <option value="todos">Todas</option>
                    {locations.map((loc) => (
                      <option key={loc} value={loc}>{loc}</option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
                </div>
              </div>

              {/* Year */}
              <div>
                <label className="block text-sm text-primary-foreground/80 mb-2 font-medium">Año</label>
                <div className="relative">
                  <select
                    value={selectedYear}
                    onChange={(e) => setSelectedYear(e.target.value)}
                    className="w-full h-10 px-3 bg-zinc-700/80 border border-zinc-600 rounded-lg text-foreground appearance-none cursor-pointer"
                  >
                    <option value="todos">Todos</option>
                    {years.map((year) => (
                      <option key={year} value={year.toString()}>{year}</option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
                </div>
              </div>

              {/* Fuel */}
              <div>
                <label className="block text-sm text-primary-foreground/80 mb-2 font-medium">Combustible</label>
                <div className="relative">
                  <select
                    value={selectedFuel}
                    onChange={(e) => setSelectedFuel(e.target.value)}
                    className="w-full h-10 px-3 bg-zinc-700/80 border border-zinc-600 rounded-lg text-foreground appearance-none cursor-pointer"
                  >
                    <option value="todos">Todos</option>
                    {fuelTypes.map((fuel) => (
                      <option key={fuel} value={fuel}>{fuel}</option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
                </div>
              </div>

              {/* Transmission */}
              <div>
                <label className="block text-sm text-primary-foreground/80 mb-2 font-medium">Transmisión</label>
                <div className="relative">
                  <select
                    value={selectedTransmission}
                    onChange={(e) => setSelectedTransmission(e.target.value)}
                    className="w-full h-10 px-3 bg-zinc-700/80 border border-zinc-600 rounded-lg text-foreground appearance-none cursor-pointer"
                  >
                    <option value="todos">Todas</option>
                    {transmissions.map((trans) => (
                      <option key={trans} value={trans}>{trans}</option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Results Grid */}
        {filteredVehicles.length > 0 ? (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredVehicles.map((vehicle, index) => (
              <div
                key={vehicle.id}
                className={`transition-all duration-500 ${isLoaded ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}`}
                style={{ transitionDelay: `${Math.min(index * 50 + 200, 800)}ms` }}
              >
                <VehicleCard vehicle={vehicle} index={index} />
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-20">
            <Car className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-foreground mb-2">
              No se encontraron vehículos
            </h3>
            <p className="text-muted-foreground mb-6">
              Intentá ajustar los filtros o buscar con otros términos
            </p>
            <Button onClick={clearFilters} variant="outline">
              Limpiar filtros
            </Button>
          </div>
        )}
      </div>
    </main>
  );
}
