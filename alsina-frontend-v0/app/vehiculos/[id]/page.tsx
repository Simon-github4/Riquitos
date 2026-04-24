"use client";

import { useParams, notFound } from "next/navigation";
import Link from "next/link";
import Image from "next/image";
import { use, useEffect, useState } from "react";
import {
  ArrowLeft,
  Calendar,
  Gauge,
  MapPin,
  Fuel,
  Settings2,
  Palette,
  Users,
  DoorOpen,
  Shield,
  Check,
  MessageCircle,
  Share2,
  Heart,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { allVehicles, type VehicleData } from "@/lib/vehicles";
import { VehicleCard } from "@/components/vehicle-card";

function VehicleDetailContent({ vehicle }: { vehicle: VehicleData }) {
  const [isLoaded, setIsLoaded] = useState(false);
  const [isFavorite, setIsFavorite] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  // Simulated multiple images (in real app would come from data)
  const images = [
    vehicle.image,
    vehicle.image.replace("q=80", "q=70"),
    vehicle.image.replace("q=80", "q=90"),
  ];

  const relatedVehicles = allVehicles
    .filter((v) => v.type === vehicle.type && v.id !== vehicle.id)
    .slice(0, 3);

  useEffect(() => {
    setIsLoaded(true);
  }, []);

  const specs = [
    { icon: Calendar, label: "Año", value: vehicle.year.toString() },
    { icon: Gauge, label: "Kilómetros", value: vehicle.kilometers },
    { icon: MapPin, label: "Ubicación", value: vehicle.location },
    { icon: Fuel, label: "Combustible", value: vehicle.fuel },
    { icon: Settings2, label: "Transmisión", value: vehicle.transmission },
    { icon: Palette, label: "Color", value: vehicle.color },
    { icon: DoorOpen, label: "Puertas", value: vehicle.doors.toString() },
    { icon: Users, label: "Pasajeros", value: vehicle.passengers.toString() },
  ];

  const isRental = vehicle.type === "alquiler";

  return (
    <main className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-background/80 backdrop-blur-md border-b border-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <Link
              href="/vehiculos"
              className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              <span className="hidden sm:inline">Volver al catálogo</span>
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

            <div className="flex items-center gap-2">
              <button
                onClick={() => setIsFavorite(!isFavorite)}
                className={`p-2 rounded-lg transition-colors ${
                  isFavorite ? "bg-primary/20 text-primary" : "text-muted-foreground hover:text-foreground"
                }`}
              >
                <Heart className={`w-5 h-5 ${isFavorite ? "fill-primary" : ""}`} />
              </button>
              <button className="p-2 rounded-lg text-muted-foreground hover:text-foreground transition-colors">
                <Share2 className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid lg:grid-cols-2 gap-8 lg:gap-12">
          {/* Image Gallery */}
          <div
            className={`transition-all duration-700 ${
              isLoaded ? "opacity-100 translate-x-0" : "opacity-0 -translate-x-12"
            }`}
          >
            {/* Main Image */}
            <div className="relative aspect-[4/3] rounded-2xl overflow-hidden bg-card mb-4 group">
              <Image
                src={images[currentImageIndex]}
                alt={vehicle.name}
                fill
                className="object-cover transition-transform duration-500 group-hover:scale-105"
                priority
              />

              {/* Type Badge */}
              <div className="absolute top-4 left-4">
                <span className="bg-primary text-primary-foreground text-sm font-semibold px-4 py-2 rounded-lg uppercase shadow-lg">
                  {vehicle.type}
                </span>
              </div>

              {/* Navigation Arrows */}
              <button
                onClick={() => setCurrentImageIndex((prev) => (prev === 0 ? images.length - 1 : prev - 1))}
                className="absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-background/80 backdrop-blur-sm rounded-full flex items-center justify-center text-foreground opacity-0 group-hover:opacity-100 transition-opacity hover:bg-background"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              <button
                onClick={() => setCurrentImageIndex((prev) => (prev === images.length - 1 ? 0 : prev + 1))}
                className="absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-background/80 backdrop-blur-sm rounded-full flex items-center justify-center text-foreground opacity-0 group-hover:opacity-100 transition-opacity hover:bg-background"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>

            {/* Thumbnail Strip */}
            <div className="flex gap-2">
              {images.map((img, index) => (
                <button
                  key={index}
                  onClick={() => setCurrentImageIndex(index)}
                  className={`relative flex-1 aspect-[4/3] rounded-lg overflow-hidden transition-all ${
                    currentImageIndex === index
                      ? "ring-2 ring-primary"
                      : "opacity-60 hover:opacity-100"
                  }`}
                >
                  <Image src={img} alt={`${vehicle.name} ${index + 1}`} fill className="object-cover" />
                </button>
              ))}
            </div>
          </div>

          {/* Vehicle Info */}
          <div
            className={`transition-all duration-700 delay-150 ${
              isLoaded ? "opacity-100 translate-x-0" : "opacity-0 translate-x-12"
            }`}
          >
            {/* Brand & Name */}
            <div className="mb-6">
              <p className="text-primary text-sm font-semibold tracking-widest uppercase mb-2">
                {vehicle.brand}
              </p>
              <h1 className="text-3xl sm:text-4xl font-black text-foreground mb-4">
                {vehicle.name}
              </h1>
              <div className="flex items-baseline gap-2">
                <span className="text-4xl font-black text-primary">
                  $ {vehicle.price}
                </span>
                {isRental && (
                  <span className="text-muted-foreground text-lg">/día</span>
                )}
              </div>
            </div>

            {/* Rental Specifics */}
            {isRental && vehicle.insurance && (
              <div className="flex flex-wrap gap-3 mb-6">
                <span className="flex items-center gap-2 bg-card border border-border px-4 py-2 rounded-lg text-sm">
                  <Shield className="w-4 h-4 text-primary" />
                  {vehicle.insurance}
                </span>
                {vehicle.franchise && (
                  <span className="bg-card border border-border px-4 py-2 rounded-lg text-sm">
                    Franquicia {vehicle.franchise}
                  </span>
                )}
              </div>
            )}

            {/* Description */}
            <p className="text-muted-foreground mb-8 leading-relaxed">
              {vehicle.description}
            </p>

            {/* Specs Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
              {specs.map((spec) => (
                <div
                  key={spec.label}
                  className="bg-card border border-border rounded-xl p-4 text-center hover:border-primary/50 transition-colors"
                >
                  <spec.icon className="w-5 h-5 text-primary mx-auto mb-2" />
                  <p className="text-xs text-muted-foreground mb-1">{spec.label}</p>
                  <p className="text-sm font-semibold text-foreground truncate">{spec.value}</p>
                </div>
              ))}
            </div>

            {/* Motor */}
            <div className="bg-card border border-border rounded-xl p-4 mb-8">
              <p className="text-sm text-muted-foreground mb-1">Motor</p>
              <p className="text-lg font-bold text-foreground">{vehicle.engine}</p>
            </div>

            {/* Features */}
            <div className="mb-8">
              <h3 className="text-lg font-semibold text-foreground mb-4">Equipamiento</h3>
              <div className="grid sm:grid-cols-2 gap-3">
                {vehicle.features.map((feature) => (
                  <div key={feature} className="flex items-center gap-3">
                    <div className="w-6 h-6 bg-primary/20 rounded-full flex items-center justify-center flex-shrink-0">
                      <Check className="w-4 h-4 text-primary" />
                    </div>
                    <span className="text-sm text-muted-foreground">{feature}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* CTA */}
            <div className="flex flex-col sm:flex-row gap-4">
              <Button
                asChild
                size="lg"
                className="flex-1 bg-primary hover:bg-primary/90 text-primary-foreground h-14 text-base"
              >
                <a
                  href={`https://wa.me/5411555512345?text=Hola! Me interesa el ${vehicle.name}`}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <MessageCircle className="mr-2 h-5 w-5" />
                  {isRental ? "CONSULTAR DISPONIBILIDAD" : "CONSULTAR AHORA"}
                </a>
              </Button>
            </div>
          </div>
        </div>

        {/* Related Vehicles */}
        {relatedVehicles.length > 0 && (
          <section className="mt-20">
            <div className="flex items-center justify-between mb-8">
              <h2 className="text-2xl sm:text-3xl font-black text-foreground">
                VEHÍCULOS RELACIONADOS
              </h2>
              <Link
                href={`/vehiculos?tipo=${vehicle.type}`}
                className="text-muted-foreground hover:text-primary transition-colors text-sm"
              >
                Ver todos
              </Link>
            </div>
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {relatedVehicles.map((v, index) => (
                <VehicleCard key={v.id} vehicle={v} index={index} />
              ))}
            </div>
          </section>
        )}
      </div>
    </main>
  );
}

export default function VehicleDetailPage() {
  const params = useParams();
  const id = params.id as string;
  
  const vehicle = allVehicles.find((v) => v.id === id);

  if (!vehicle) {
    notFound();
  }

  return <VehicleDetailContent vehicle={vehicle} />;
}
