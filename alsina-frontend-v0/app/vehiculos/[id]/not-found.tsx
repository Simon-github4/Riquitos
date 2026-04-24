import Link from "next/link";
import { Car } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function VehicleNotFound() {
  return (
    <main className="min-h-screen bg-background flex items-center justify-center">
      <div className="text-center px-4">
        <Car className="w-20 h-20 text-muted-foreground mx-auto mb-6" />
        <h1 className="text-4xl font-black text-foreground mb-4">
          Vehículo no encontrado
        </h1>
        <p className="text-muted-foreground mb-8 max-w-md mx-auto">
          El vehículo que buscás no existe o ya no está disponible. 
          Explorá nuestro catálogo para encontrar otras opciones.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Button asChild>
            <Link href="/vehiculos">Ver catálogo</Link>
          </Button>
          <Button asChild variant="outline">
            <Link href="/">Volver al inicio</Link>
          </Button>
        </div>
      </div>
    </main>
  );
}
